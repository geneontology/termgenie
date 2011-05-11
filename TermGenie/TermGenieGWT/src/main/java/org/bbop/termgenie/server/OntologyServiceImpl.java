package org.bbop.termgenie.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.TermSuggestion;
import org.bbop.termgenie.shared.GWTTermGenerationParameter.GWTOntologyTerm;
import org.bbop.termgenie.solr.SimpleSolrClient;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {

	@Override
	public List<String> getAvailableOntologies() {
		// TODO Where do you get the list of available ontologies from?
		return Arrays.asList("GeneOntology", "Test1","Test2");
	}

	@Override
	public List<TermSuggestion> autocompleteQuery(String query, String ontologyName, int max) {
		if (query.equals("t1")) {
			TermSuggestion s1 = new TermSuggestion("test1", new GWTOntologyTerm("GeneOntology",
					"GO:0000001"), null, null);
			TermSuggestion s11 = new TermSuggestion("test11", new GWTOntologyTerm("GeneOntology",
			"GO:0000011"), null, null);
			return Arrays.asList(s1,s11);
		}
		else if (query.equals("t2")) {
			TermSuggestion s2 = new TermSuggestion("test2", new GWTOntologyTerm("GeneOntology",
			"GO:0000002"), null, null);
			return Arrays.asList(s2);
		}
		Ontology ontology = getOntology(ontologyName);
		if (ontology == null) {
			// unknown ontology, do nothing
			return null;
		}
		List<OntologyTerm> autocompleteList = autocomplete(query, ontology, max);
		if (autocompleteList == null || autocompleteList.isEmpty()) {
			// no terms found, do nothing
			return null;
		}
		List<TermSuggestion> suggestions = new ArrayList<TermSuggestion>();
		for (OntologyTerm ontologyTerm : autocompleteList) {
			TermSuggestion suggestion = createSuggestion(ontology, ontologyTerm);
			if (suggestion != null) {
				suggestions.add(suggestion);
			}
		}
		return suggestions;
	}

	private Ontology getOntology(String ontology) {
		// TODO
		return null;
	}
	
	private String getOntologyName(Ontology ontology) {
		return ontology.getUniqueName();
	}
	
	private TermSuggestion createSuggestion(Ontology ontology, OntologyTerm term) {
		GWTOntologyTerm identifier = new GWTOntologyTerm(getOntologyName(ontology), term.getId());
		return new TermSuggestion(term.getLabel(), identifier , term.getDescription(), term.getReferenceLink());
	}
	
	protected List<OntologyTerm> autocomplete(String query, Ontology ontology, int max) {
		return SolrClient.autocomplete(query, ontology, max);
	}

	private static class SolrClient {
		
		private static final SimpleSolrClient client = new SimpleSolrClient();
		
		/**
		 * @param query
		 * @param ontology
		 * @param maxCount
		 * @return list of terms
		 */
		public static List<OntologyTerm> autocomplete(String query, Ontology ontology, int maxCount) {
			return client.suggestTerms(query, ontology, maxCount);
		}
	}

}
