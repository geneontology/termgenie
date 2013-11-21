package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;

/**
 * Information about the available {@link Ontology}.
 */
public interface OntologyConfiguration {

	/**
	 * Get the Configuration details of the ontology.
	 * 
	 * @return ontology configuration map
	 */
	public Ontology getOntologyConfiguration();
}
