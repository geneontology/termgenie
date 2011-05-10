package org.bbop.termgenie.server;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.TermSuggestion;
import org.bbop.termgenie.shared.GWTTermGenerationParameter.OntologyTerm;

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
		if (query.equals("t1")) {
			TermSuggestion s1 = new TermSuggestion("test1", new OntologyTerm("GeneOntology",
					"GO:0000001"), null, null);
			TermSuggestion s11 = new TermSuggestion("test11", new OntologyTerm("GeneOntology",
			"GO:0000011"), null, null);
			return Arrays.asList(s1,s11);
		}
		else if (query.equals("t2")) {
			TermSuggestion s2 = new TermSuggestion("test2", new OntologyTerm("GeneOntology",
			"GO:0000002"), null, null);
			return Arrays.asList(s2);
		}
		return null;
	}

}
