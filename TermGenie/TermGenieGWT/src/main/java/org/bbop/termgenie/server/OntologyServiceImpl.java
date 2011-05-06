package org.bbop.termgenie.server;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.TermSuggestion;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {

	@Override
	public List<String> getAvailableOntologies() {
		// TODO Where do you get the list of available ontologies from?
		return Arrays.asList("GeneOntology", "Test1","Test2");
	}

	@Override
	public List<TermSuggestion> autocompleteQuery(String query, String ontology) {
		// TODO Auto-generated method stub
		return null;
	}

}
