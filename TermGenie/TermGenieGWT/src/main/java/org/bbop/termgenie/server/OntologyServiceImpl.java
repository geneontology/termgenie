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

import owltools.graph.OWLGraphWrapper;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {

	private static final String GENE_ONTOLOGY_NAME = "GeneOntology";

	@Override
	public List<String> getAvailableOntologies() {
		// TODO Where do you get the list of available ontologies from?
		return Arrays.asList(GENE_ONTOLOGY_NAME, "Test1","Test2");
	}

	@Override
	public List<TermSuggestion> autocompleteQuery(String query, String ontologyName, int max) {
		// sanity checks
		if (query == null || query.length() <= 2  || ontologyName == null) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}
		
		//  get ontology
		Ontology ontology = getOntology(ontologyName);
		if (ontology == null) {
			// unknown ontology, do nothing
			return null;
		}
		
		// query for terms
		List<OntologyTerm> autocompleteList = autocomplete(query, ontology, max);
		if (autocompleteList == null || autocompleteList.isEmpty()) {
			// no terms found, do nothing
			return null;
		}
		
		// prepare suggestions
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
		// TODO remove hard-coded mapping and support more ontologies
		if (ontology.startsWith(GENE_ONTOLOGY_NAME)) {
			String branch = null;
			int length = GENE_ONTOLOGY_NAME.length();
			if (ontology.length() > length + 1 && ontology.charAt(length) == '|') {
				branch = ontology.substring(length + 1);
			} 
			return new SimpleOntology(GENE_ONTOLOGY_NAME, branch);
		}
		return null;
	}
	
	private final class SimpleOntology extends Ontology {
		
		private final String name;
		private final String branch;
	
		/**
		 * @param name
		 * @param branch
		 */
		private SimpleOntology(String name, String branch) {
			super();
			this.name = name;
			this.branch = branch;
		}
	
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}
	
		@Override
		public String getUniqueName() {
			return name;
		}
	
		@Override
		public String getBranch() {
			return branch;
		}
	}

	private String getOntologyName(Ontology ontology) {
		StringBuilder sb = new StringBuilder();
		sb.append(ontology.getUniqueName());
		String branch = ontology.getBranch();
		if (branch != null) {
			sb.append('|');
			sb.append(branch);
		}
		return sb.toString();
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
