package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

public interface OntologyLoader {

	/**
	 * Get all configured ontologies.
	 * 
	 * @return ontology managers
	 */
	public List<OntologyTaskManager> getOntologies();
	
	/**
	 * Get a selected ontology configuration, useful for testing.
	 * 
	 * @param configuredOntology parameter
	 * @return ontology manager
	 */
	public OntologyTaskManager getOntology(ConfiguredOntology configuredOntology);
	
}
