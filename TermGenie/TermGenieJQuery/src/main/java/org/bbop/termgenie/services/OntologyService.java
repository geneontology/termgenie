package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonTermSuggestion;

public interface OntologyService  {

	public String[] getAvailableOntologies();
	
	public JsonTermSuggestion[] autocompleteQuery(String query, String[] ontologies, int max);
}
