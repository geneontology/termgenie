package org.bbop.termgenie.servlets;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTerm;
import org.bbop.termgenie.rpc.JSONMethod;
import org.bbop.termgenie.rpc.JsonRpcServlet;
import org.bbop.termgenie.tools.ImplementationFactory;
import org.bbop.termgenie.tools.OntologyTools;

@SuppressWarnings("serial")
public class OntologyServlet extends JsonRpcServlet {

	private static final OntologyTools ontologyTools = ImplementationFactory.getOntologyTools();
	private static final OntologyTermSuggestor suggestor = ImplementationFactory.getOntologyTermSuggestor();
	
	@JSONMethod
	public List<String> getAvailableOntologies() {
		return ontologyTools.getAvailableOntologyNames();
	}

	@JSONMethod
	public List<JsonTermSuggestion> autocompleteQuery(String query, String[] ontologyNames, int max) {
		// sanity checks
		if (query == null || query.length() <= 2  || ontologyNames == null || ontologyNames.length == 0) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}
		
		List<JsonTermSuggestion> suggestions = new ArrayList<JsonTermSuggestion>();
		
		for (int i = 0; i < ontologyNames.length; i++) {
			//  get ontology
			Ontology ontology = ontologyTools.getOntology(ontologyNames[i]);
			if (ontology == null) {
				// unknown ontology, do nothing
				continue;
			}
			// query for terms
			List<OntologyTerm> autocompleteList = suggestor.suggestTerms(query, ontology, max);
			if (autocompleteList == null || autocompleteList.isEmpty()) {
				// no terms found, do nothing
				continue;
			}
			// prepare suggestions
			List<JsonTermSuggestion> csuggestions = new ArrayList<JsonTermSuggestion>(autocompleteList.size());
			for (int j = 0; j < autocompleteList.size(); j++) {
				JsonTermSuggestion suggestion = createSuggestion(ontology, autocompleteList.get(j));
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
	
	private JsonTermSuggestion createSuggestion(Ontology ontology, OntologyTerm term) {
		String ontologyName = ontologyTools.getOntologyName(ontology);
		JsonOntologyTerm identifier = new JsonOntologyTerm(ontologyName, term.getId());
		return new JsonTermSuggestion(term.getLabel(), identifier , term.getDefinition(), term.getSynonyms());
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
