package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.tools.Pair;

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
	public Pair<String, Integer> getNewId(Ontology ontology);

	/**
	 * Try to rollback to the given integer. This is useful while attempting a
	 * commit, which fails with a recoverable error.
	 * 
	 * @param ontology
	 * @param id
	 * @return boolean, true if the rollback was successful.
	 */
	public boolean rollbackId(Ontology ontology, Integer id);
}
