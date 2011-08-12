package org.bbop.termgenie.core.rules;

import java.util.Collection;

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
	 * @param reasonerName
	 * @return taskManager
	 */
	public ReasonerTaskManager getTaskManager(OWLGraphWrapper ontology, String reasonerName);

	/**
	 * Retrieve a collection of all available reasoners in this factory.
	 * 
	 * @return reasoner names
	 */
	public Collection<String> getSupportedReasoners();
}
