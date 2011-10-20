package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.tools.OntologyTools;

import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OntologyServiceImpl implements OntologyService {

	private final OntologyTools ontologyTools;
	private final OntologyTermSuggestor suggestor;

	/**
	 * @param ontologyTools
	 * @param suggestor
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
			List<OntologyTerm<ISynonym, IRelation>> autocompleteList = suggestor.suggestTerms(query, ontology, max);
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
		return suggestions.toArray(new JsonTermSuggestion[suggestions.size()]);
	}

	private JsonTermSuggestion createSuggestion(Ontology ontology, OntologyTerm<ISynonym, IRelation> term) {
		String ontologyName = ontologyTools.getOntologyName(ontology);
		JsonOntologyTermIdentifier identifier = new JsonOntologyTermIdentifier(ontologyName, term.getId());
		return new JsonTermSuggestion(term.getLabel(), identifier, term.getDefinition(), getSynonyms(term));
	}

	private String[] getSynonyms(OntologyTerm<ISynonym, IRelation> term) {
		List<ISynonym> synonyms = term.getSynonyms();
		if (synonyms != null && !synonyms.isEmpty()) {
			String[] strings = new String[synonyms.size()];
			for (int i = 0; i < strings.length; i++) {
				strings[i] = synonyms.get(i).getLabel();
			}
			return strings;
		}
		return null;
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
