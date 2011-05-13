package org.bbop.termgenie.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("ontology")
public interface OntologyService extends RemoteService  {

	public List<String> getAvailableOntologies();
	
	public List<TermSuggestion> autocompleteQuery(String query, String[] ontologies, int max);
}
