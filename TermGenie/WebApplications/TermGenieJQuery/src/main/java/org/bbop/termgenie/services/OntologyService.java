package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonTermSuggestion;

public interface OntologyService {

	/**
	 * Retrieve the ontology names, for which patterns are known to the system.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @return ontologyNames
	 */
	public String[] availableOntologies(String sessionId);

	/**
	 * Auto complete the query with terms in the specified ontologies. Return
	 * only max number of results.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param query
	 * @param ontologies
	 * @param max
	 * @return term suggestions
	 */
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String[] ontologies,
			int max);
}
