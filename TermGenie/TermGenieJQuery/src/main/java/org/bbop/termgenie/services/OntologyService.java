package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonTermSuggestion;

public interface OntologyService  {

	public String[] availableOntologies();
	
	public JsonTermSuggestion[] autocomplete(String query, String[] ontologies, int max);
}
