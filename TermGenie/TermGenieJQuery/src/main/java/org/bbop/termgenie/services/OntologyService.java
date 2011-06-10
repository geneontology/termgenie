package org.bbop.termgenie.services;

import java.util.List;

import org.bbop.termgenie.data.JsonTermSuggestion;

public interface OntologyService  {

	public List<String> getAvailableOntologies();
	
	public List<JsonTermSuggestion> autocompleteQuery(String query, List<String> ontologies, int max);
}
