package org.bbop.termgenie.server;

import java.util.ArrayList;
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
		return OntologyTools.instance.getAvailableOntologyNames();
	}

	@Override
	public List<TermSuggestion> autocompleteQuery(String query, String[] ontologyNames, int max) {
		// sanity checks
		if (query == null || query.length() <= 2  || ontologyNames == null || ontologyNames.length == 0) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}
		
		List<TermSuggestion> suggestions = new ArrayList<TermSuggestion>();
		
		for (int i = 0; i < ontologyNames.length; i++) {
			//  get ontology
			Ontology ontology = OntologyTools.instance.getOntology(ontologyNames[i]);
			if (ontology == null) {
				// unknown ontology, do nothing
				continue;
			}
			// query for terms
			List<OntologyTerm> autocompleteList = autocomplete(query, ontology, max);
			if (autocompleteList == null || autocompleteList.isEmpty()) {
				// no terms found, do nothing
				continue;
			}
			// prepare suggestions
			List<TermSuggestion> csuggestions = new ArrayList<TermSuggestion>(autocompleteList.size());
			for (int j = 0; j < autocompleteList.size(); j++) {
				TermSuggestion suggestion = createSuggestion(ontology, autocompleteList.get(j));
				if (suggestion != null) {
					csuggestions.add(suggestion);
				}
			}
			mergeLists(suggestions, csuggestions);
		}
		if (suggestions.size() > max) {
			suggestions = suggestions.subList(0, max);
		}
		return suggestions;
	}

	private TermSuggestion createSuggestion(Ontology ontology, OntologyTerm term) {
		String ontologyName = OntologyTools.instance.getOntologyName(ontology);
		GWTOntologyTerm identifier = new GWTOntologyTerm(ontologyName, term.getId());
		return new TermSuggestion(term.getLabel(), identifier , term.getDescription(), term.getSynonyms());
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
	
	
	/**
	 * Merge two list by inserting it after the corresponding element in the
	 * target list. See {@link OntologyServiceImplTest} for details.
	 * 
	 * @param <T>
	 * @param target
	 * @param insert
	 * @return merged list
	 */
	static <T> List<T> mergeLists(List<T> target, List<T> insert) {
		if (insert.isEmpty()) {
			return target;
		}
		if (target.isEmpty()) {
			target.addAll(insert);
			insert.clear();
			return target;
		}

		int targetLength = target.size();
		int insertLength = insert.size();
		if (targetLength == insertLength) {
			target.add(insert.remove(insertLength - 1));
			insertLength = insert.size();
		} else if (targetLength < insertLength) {
			int fromIndex = Math.max(0, insertLength - (insertLength - targetLength + 1));
			List<T> subList = new ArrayList<T>(insert.subList(fromIndex, insertLength));
			target.addAll(subList);
			insert.removeAll(subList);
			insertLength = insert.size();
		}
		for (int i = insert.size() - 1; i >= 0; i--) {
			target.add(i + 1, insert.remove(i));
		}
		return target;
	}

}
