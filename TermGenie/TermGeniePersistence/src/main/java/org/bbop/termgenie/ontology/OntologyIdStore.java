package org.bbop.termgenie.ontology;

import java.text.DecimalFormat;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.entities.OntologyIdInfo;
import org.bbop.termgenie.tools.Pair;

/**
 * Store of ontology identifiers and corresponding patterns.
 */
class OntologyIdStore {

	private static final Logger logger = Logger.getLogger(OntologyIdStore.class);

	/**
	 * Create a new store with a given {@link OntologyIdStoreConfiguration} and
	 * {@link EntityManagerFactory}.
	 * 
	 * @param configuration the configuration
	 * @param entityManager the entity manager for persistence
	 */
	OntologyIdStore(OntologyIdStoreConfiguration configuration, EntityManagerFactory entityManagerFactory) {
		super();
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			Map<String, OntologyIdInfo> infos = configuration.getInfos();
			for (String ontologyName : infos.keySet()) {
				OntologyIdInfo info = infos.get(ontologyName);
				OntologyIdInfo infoPersitent = getInfo(ontologyName, entityManager);
				if (infoPersitent != null) {
					if (info.getMaximum() != infoPersitent.getMaximum()) {
						// if different log message and update
						warn("Contradicting configuration: current ID range end (" + infoPersitent.getMaximum() + ") is different than the new configured one: " + desc(info));
						infoPersitent.setMaximum(info.getMaximum());
					}
					if (!info.getPattern().equals(infoPersitent.getPattern())) {
						warn("Contradicting configuration: current ID pattern (" + infoPersitent.getPattern() + ") is different than the new configured one: " + desc(info));
						infoPersitent.setPattern(info.getPattern());
					}
					entityManager.persist(infoPersitent);
				}
				else {
					// create a new entry
					entityManager.persist(info);
				}
			}
			entityManager.getTransaction().commit();
		} finally {
			entityManager.close();
		}
	}

	/**
	 * @param ontology
	 * @param entityManager
	 * @return newId
	 */
	Pair<String, Integer> getNewId(Ontology ontology, EntityManagerFactory entityManagerFactory) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OntologyIdInfo info = getInfo(ontology, entityManager);
			int current = info.getCurrent();
			int maximum = info.getMaximum();
			int next = current + 1;
			if (next >= maximum) {
				entityManager.getTransaction().rollback();
				error("Upper limit (" + Integer.toString(maximum) + ") of the ID range reached for ontology: " + ontology.getName());
			}
			info.setCurrent(next);
			String pattern = info.getPattern();
			DecimalFormat nf = new DecimalFormat(pattern);
			entityManager.merge(info);
			entityManager.getTransaction().commit();
			return new Pair<String, Integer>(nf.format(current), next);
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the next id to the indicated id to rollback.
	 * 
	 * @param ontology
	 * @param id
	 * @param entityManager
	 * @return
	 */
	boolean rollbackId(Ontology ontology, Integer id, EntityManagerFactory entityManagerFactory) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			OntologyIdInfo info = getInfo(ontology, entityManager);
			int current = info.getCurrent();
			if (current > id) {
				info.setCurrent(id);
				entityManager.getTransaction().commit();
				return true;
			}
			return false;
		} finally {
			entityManager.close();
		}
	}

	private OntologyIdInfo getInfo(Ontology ontology, EntityManager entityManager) {
		return getInfo(ontology.getName(), entityManager);
	}

	private OntologyIdInfo getInfo(String ontologyName, EntityManager entityManager) {
		return entityManager.find(OntologyIdInfo.class, ontologyName);
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
	private static String desc(OntologyIdInfo info) {
		StringBuilder sb = new StringBuilder();
		sb.append("ontology: ");
		sb.append(info.getOntologyName());
		sb.append(" start: ");
		sb.append(info.getCurrent());
		sb.append(" max: ");
		sb.append(info.getMaximum());
		if (info.getPattern() != null) {
			sb.append(" pattern: ");
			sb.append(info.getPattern());
		}
		return sb.toString();
	}

	/*
	 * helper to increase readability of the code
	 */
	private void warn(String message) {
		logger.warn(message);
	}

	/*
	 * helper to increase readability of the code
	 */
	private void error(String message) {
		throw new OntologyIdStoreException(message);
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
