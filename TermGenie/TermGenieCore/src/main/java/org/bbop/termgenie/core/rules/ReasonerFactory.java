package org.bbop.termgenie.core.rules;

import owltools.graph.OWLGraphWrapper;

/**
 * Wrap the configuration and creation of reasoners. Encapsulate the reasoning
 * for one ontology in a {@link ReasonerTaskManager} to isolate different
 * requests from each other.
 */
public interface ReasonerFactory {

	/**
	 * Get a task manager for a given ontology, using a default reasoner
	 * implementation.
	 * 
	 * @param ontology
	 * @return taskManager
	 */
	public ReasonerTaskManager getDefaultTaskManager(OWLGraphWrapper ontology);

	/**
	 * Get a task manager for a given ontology and reasoner.
	 * 
	 * @param ontology
	 * @return taskManager
	 */
	public ReasonerTaskManager getTaskManager(OWLGraphWrapper ontology);

	/**
	 * Update the buffered reasoners to reload the underlying ontology.
	 * 
	 * @param id ontology id
	 */
	public void updateBuffered(String id);
}
