package org.bbop.termgenie.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.io.ListHelper;
import org.bbop.termgenie.ontology.entities.OntologyIdInfo;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * TODO Re-design this to allow pending IDs, which have a timeout and can be
 * reused (a day later?).
 */
@Singleton
public class OntologyIdStore {

	private static final Logger logger = Logger.getLogger(OntologyIdStore.class);

	/**
	 * Create a new store with a given configuration and {@link EntityManager}.
	 * The configuration is expected to be in the following format:
	 * <ul>
	 * <li>Each line represents one ontology</li>
	 * <li>A line a for fields, separated by tabulators</li>
	 * <li>Field 1: ontology name</li>
	 * <li>Field 2: id pattern</li>
	 * <li>Field 3: ID range start</li>
	 * <li>Field 4: ID range end</li>
	 * </ul>
	 * 
	 * @param inputStream stream containing the configuration
	 * @param entityManager the entity manager for persistence
	 */
	@Inject
	OntologyIdStore(InputStream inputStream, EntityManager entityManager) {
		super();
		entityManager.getTransaction().begin();
		try {
			int lineCount = 0;
			LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			while (lineIterator.hasNext()) {
				String string = lineIterator.next();
				lineCount += 1;
				if (string.startsWith("#") || string.length() < 7) {
					// ignore comments
					// ignore line with too few chars
					// (4 cols + 3 separators = 7 chars)
					continue;
				}
				List<String> fields = ListHelper.parseString(string, '\t');
				if (fields.size() == 4) {
					String ontologyName = fields.get(0);
					String pattern = fields.get(1);
					int start = Integer.parseInt(fields.get(2));
					int max = Integer.parseInt(fields.get(3));
					if (max <= start) {
						error("Invalid configuration line #" + lineCount + ": the start of the ID range must be smaller than the end: " + desc(ontologyName,
								start,
								max,
								pattern));
					}

					OntologyIdInfo info = getInfo(ontologyName, entityManager);
					if (info != null) {
						// check if content is consistent with stored info
						int current = info.getCurrent();
						if (current < start) {
							error("Contradicting configuration line #" + lineCount + ": current ID range start (" + current + ") is smaller than the new configured one: " + desc(ontologyName,
									start,
									max,
									pattern));
						}
						else if (current < start) {
							info.setCurrent(start);
						}
						if (max != info.getMaximum()) {
							// if different log message and update
							warn("Contradicting configuration line #" + lineCount + ": current ID range end (" + info.getMaximum() + ") is different than the new configured one: " + desc(ontologyName,
									start,
									max,
									pattern));
							info.setMaximum(max);
						}
						if (!pattern.equals(info.getPattern())) {
							warn("Contradicting configuration line #" + lineCount + ": current ID range end (" + info.getPattern() + ") is different than the new configured one: " + desc(ontologyName,
									start,
									max,
									pattern));
							info.setPattern(pattern);
						}
					}
					else {
						// create a new entry
						info = new OntologyIdInfo();
						info.setCurrent(start);
						info.setMaximum(max);
						info.setOntologyName(ontologyName);
						info.setPattern(pattern);
						entityManager.persist(info);
					}
				}
				else {
					// warn, skip line
					warn("Skipping line #" + lineCount + " in configuration with unexpected number of fields: " + fields.size());
				}
			}
			entityManager.getTransaction().commit();
		} catch (IOException exception) {
			entityManager.getTransaction().rollback();
			error("Error loading config resource.", exception);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}

	}

	/**
	 * @param ontology
	 * @param entityManager
	 * @return newId
	 */
	String getNewId(Ontology ontology, EntityManager entityManager) {
		entityManager.getTransaction().begin();
		OntologyIdInfo info = getInfo(ontology, entityManager);
		int current = info.getCurrent();
		int maximum = info.getMaximum();
		int next = current + 1;
		if (next >= maximum) {
			error("Upper limit (" + Integer.toString(maximum) + ") of the ID range reached for ontology: " + ontology.getUniqueName());
		}
		info.setCurrent(next);
		String pattern = info.getPattern();
		DecimalFormat nf = new DecimalFormat(pattern);
		entityManager.getTransaction().commit();
		return nf.format(current);
	}

	private OntologyIdInfo getInfo(Ontology ontology, EntityManager entityManager) {
		return getInfo(ontology.getUniqueName(), entityManager);
	}

	private OntologyIdInfo getInfo(String ontologyName, EntityManager entityManager) {

		TypedQuery<OntologyIdInfo> query = entityManager.createQuery("SELECT info FROM OntologyIdInfo info " + "WHERE info.ontologyName = ?1",
				OntologyIdInfo.class);
		query.setParameter(1, ontologyName);
		List<OntologyIdInfo> results = query.getResultList();
		if (results != null && !results.isEmpty()) {
			OntologyIdInfo info = results.get(0);
			return info;
		}
		return null;
	}

	// --------------------- Helper methods and classes ---------------------

	/**
	 * Create a description string for the given fields.
	 * 
	 * @param name
	 * @param start
	 * @param max
	 * @param pattern
	 * @return description string
	 */
	private static String desc(String name, int start, int max, String pattern) {
		StringBuilder sb = new StringBuilder();
		sb.append("ontology: ");
		sb.append(name);
		sb.append(" start: ");
		sb.append(start);
		sb.append(" max: ");
		sb.append(max);
		if (pattern != null) {
			sb.append(" pattern: ");
			sb.append(pattern);
		}
		return sb.toString();
	}

	/*
	 * helper to increase readability of the code
	 */
	void warn(String message) {
		logger.warn(message);
	}

	/*
	 * helper to increase readability of the code
	 */
	void error(String message) {
		throw new OntologyIdStoreException(message);
	}

	/*
	 * helper to increase readability of the code
	 */
	void error(String message, Throwable cause) {
		throw new OntologyIdStoreException(message, cause);
	}

	/**
	 * Provides a custom {@link RuntimeException} to allow optional handling of
	 * runtime errors, specific to this class.
	 */
	public static class OntologyIdStoreException extends RuntimeException {

		// generated
		private static final long serialVersionUID = -2422975927303309392L;

		/**
		 * @param message
		 */
		public OntologyIdStoreException(String message) {
			super(message);
		}

		/**
		 * @param message
		 * @param cause
		 */
		public OntologyIdStoreException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
