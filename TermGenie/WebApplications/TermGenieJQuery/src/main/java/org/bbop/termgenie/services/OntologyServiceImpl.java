package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.tools.OntologyTools;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OntologyServiceImpl implements OntologyService {

	private final OntologyTools ontologyTools;
	private final OntologyTermSuggestor suggestor;

	/**
	 * @param ontologyTools
	 * @param suggestor
	 * @param multiOntologyTaskManager
	 */
	@Inject
	OntologyServiceImpl(OntologyTools ontologyTools, OntologyTermSuggestor suggestor) {
		super();
		this.ontologyTools = ontologyTools;
		this.suggestor = suggestor;
	}

	@Override
	public String[] availableOntologies(String sessionId) {
		return ontologyTools.getAvailableOntologyNames();
	}

	@Override
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String[] ontologyNames,
			int max)
	{
		// sanity checks
		if (query == null || query.length() <= 2 || ontologyNames == null || ontologyNames.length == 0) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}

		List<JsonTermSuggestion> suggestions = new ArrayList<JsonTermSuggestion>();

		for (String ontologyName : ontologyNames) {
			// get ontology
			Ontology ontology = ontologyTools.getOntology(ontologyName);
			if (ontology == null) {
				// unknown ontology, do nothing
				continue;
			}
			// query for terms
			List<TermSuggestion> autocompleteList = suggestor.suggestTerms(query, ontology, max);
			if (autocompleteList == null || autocompleteList.isEmpty()) {
				// no terms found, do nothing
				continue;
			}
			// prepare suggestions
			List<JsonTermSuggestion> current = new ArrayList<JsonTermSuggestion>(autocompleteList.size());
			//String ontologyName = ontologyTools.getOntologyName(ontology);
			for (TermSuggestion termSuggestion : autocompleteList) {
				JsonOntologyTermIdentifier jsonId = new JsonOntologyTermIdentifier(ontologyTools.getOntologyName(ontology), termSuggestion.getIdentifier());
				current.add(new JsonTermSuggestion(termSuggestion.getLabel(), jsonId , termSuggestion.getDescription(), termSuggestion.getSynonyms()));
			}
			mergeLists(suggestions, current);
		}
		if (suggestions.size() > max) {
			suggestions = suggestions.subList(0, max);
		}
		return suggestions.toArray(new JsonTermSuggestion[suggestions.size()]);
	}
	
	/**
	 * Merge two list by inserting it after the corresponding element in the
	 * target list.
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
		}
		else if (targetLength < insertLength) {
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
