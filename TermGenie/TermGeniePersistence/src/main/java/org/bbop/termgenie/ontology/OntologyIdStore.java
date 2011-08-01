package org.bbop.termgenie.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.io.ListHelper;
import org.bbop.termgenie.ontology.entities.OntologyIdInfo;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * TODO Re-design this to allow pending IDs, which have a timeout and can be reused (a day later?).
 */
@Singleton
public class OntologyIdStore extends ResourceLoader {

	@Inject
	OntologyIdStore(String configResource, EntityManager entityManager) {
		super();
		InputStream inputStream = null;
		entityManager.getTransaction().begin();
		try {
			inputStream = loadResource(configResource);
			LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			while (lineIterator.hasNext()) {
				String string = lineIterator.next();
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
					if (max == start) {
						// error
					}
					else if (max > start) {
						// error
					}
					
					OntologyIdInfo info = getInfo(ontologyName, entityManager);
					if (info != null) {
						// check if content is consistent with stored info
						int current = info.getCurrent();
						if (current < start) {
							// error
						} else if (current < start) {
							info.setCurrent(start);
						}
						if (max != info.getMaximum()) {
							// if different log message and update
							info.setMaximum(max);
						}
						if (!pattern.equals(info.getPattern())) {
							// if different log message and update
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
				}
			}
			entityManager.getTransaction().commit();
		} catch (IOException exception) {
			entityManager.getTransaction().rollback();
			throw new RuntimeException("Error loading config resource.", exception);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		
	}
	
	String getNewId(Ontology ontology, EntityManager entityManager) {
		entityManager.getTransaction().begin();
		OntologyIdInfo info = getInfo(ontology, entityManager);
		int current = info.getCurrent();
		int maximum = info.getMaximum();
		int next = current + 1;
		if (next >= maximum) {
			throw new RuntimeException("Upper limit ("+Integer.toString(maximum)+") of the ID range reached for ontology: "+ontology.getUniqueName());
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
		
		TypedQuery<OntologyIdInfo> query = entityManager.createQuery(
				"SELECT o FROM :target infos " +
                "WHERE infos.ontologyName = :ontologyName", OntologyIdInfo.class);
		query.setParameter("target", OntologyIdInfo.class.getSimpleName());
		query.setParameter("ontologyName", ontologyName);
		List<OntologyIdInfo> results = query.getResultList();
		if (results != null && !results.isEmpty()) {
			OntologyIdInfo info = results.get(0);
			return info;
		}
		return null;
	}
}
