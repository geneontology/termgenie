package org.bbop.termgenie.ontology;

import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Singleton;

/**
 * Methods for removing terms and too complex expressions in ontologies.
 */
public interface OntologyCleaner {

	/**
	 * Clean the given {@link OBODoc} from un-wanted clauses. WARNING: the
	 * {@link OBODoc} itself will be modified.
	 * 
	 * @param ontology
	 * @param obodoc
	 */
	public void cleanOBOOntology(String ontology, OBODoc obodoc);
	
	@Singleton
	public static class NoopOntologyCleaner implements OntologyCleaner {

		@Override
		public void cleanOBOOntology(String ontology, OBODoc obodoc) {
			// intentionally empty
		}
	}
}
