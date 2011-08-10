package org.bbop.termgenie.ontology;

import java.util.Map;

import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

/**
 * Information about the available {@link ConfiguredOntology}.
 */
public interface OntologyConfiguration {

	/**
	 * Get the Configurations of the ontologies.
	 * 
	 * @return ontology configuration map
	 */
	public Map<String, ConfiguredOntology> getOntologyConfigurations();
}
