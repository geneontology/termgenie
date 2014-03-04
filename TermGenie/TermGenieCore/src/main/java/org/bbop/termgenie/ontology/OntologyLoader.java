package org.bbop.termgenie.ontology;


public interface OntologyLoader {

	/**
	 * Get all configured ontologies.
	 * 
	 * @return ontology managers
	 */
	public OntologyTaskManager getOntologyManager();
	
	/**
	 * Tell the loader to reload all ontologies from source.
	 */
	public void reloadOntologies();

}
