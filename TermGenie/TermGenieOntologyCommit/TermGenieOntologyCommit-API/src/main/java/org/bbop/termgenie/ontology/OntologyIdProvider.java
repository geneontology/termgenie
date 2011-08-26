package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;

/**
 * Interface for a provider of new ontology identifiers.
 */
public interface OntologyIdProvider {

	/**
	 * Create a new identifier for the given ontology.
	 * 
	 * @param ontology
	 * @return ontologyIdString
	 */
	public String getNewId(Ontology ontology);
}
