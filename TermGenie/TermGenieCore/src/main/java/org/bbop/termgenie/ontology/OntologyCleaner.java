package org.bbop.termgenie.ontology;

import org.obolibrary.oboformat.model.OBODoc;

/**
 * Methods for removing terms and too complexing expressions in ontologies.
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
}
