package org.bbop.termgenie.core.rules;

import org.bbop.termgenie.core.process.ProcessState;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

/**
 * Wrap the configuration and creation of a reasoner. Encapsulate the reasoning
 * for one ontology in a {@link SharedReasoner} to isolate different
 * requests from each other.
 */
public interface ReasonerFactory {

	/**
	 * Get a task manager for a given ontology and reasoner.
	 * 
	 * @param ontology
	 * @return taskManager
	 */
	public SharedReasoner getSharedReasoner(OWLGraphWrapper ontology);

	public OWLReasoner createReasoner(OWLOntology ontology, ProcessState state);

	/**
	 * Use this method to retrieve a name of the reasoner implementation for
	 * info purposes,
	 * 
	 * @return name of the reasoner implementation
	 */
	public String getReasonerName();

	/**
	 * Use this method to retrieve a version string for the reasoner
	 * implementation for info purposes.
	 * 
	 * @return version string
	 */
	public String getReasonerVersion();

	/**
	 * Cleanup method
	 */
	public void dispose();

}
