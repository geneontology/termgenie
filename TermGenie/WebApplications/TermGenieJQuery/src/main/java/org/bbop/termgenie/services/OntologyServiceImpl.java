package org.bbop.termgenie.services;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.ontology.OntologyLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OntologyServiceImpl implements OntologyService {

	private final OntologyTermSuggestor suggestor;
	private final String availableOntology;

	@Inject
	OntologyServiceImpl(OntologyTermSuggestor suggestor, OntologyLoader loader) {
		super();
		this.suggestor = suggestor;
		this.availableOntology = loader.getOntologyManager().getOntology().getName();
	}

	@Override
	public JsonOntologyStatus getOntologyStatus() {
		JsonOntologyStatus status = new JsonOntologyStatus();
		status.setOkay(true);
		status.setOntology(availableOntology);
		return status;
	}

	@Override
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String ontologyName,
			int max)
	{
		ontologyName = StringUtils.trimToNull(ontologyName);
		// sanity checks
		if (query == null || query.length() <= 2 || ontologyName == null) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}

		// query for terms
		List<TermSuggestion> autocompleteList = suggestor.suggestTerms(query, ontologyName, max);
		if (autocompleteList == null || autocompleteList.isEmpty()) {
			// no terms found
			return new JsonTermSuggestion[0];
		}
		// prepare suggestions
		JsonTermSuggestion[] result = new JsonTermSuggestion[autocompleteList.size()];
		for (int i = 0; i < result.length; i++) {
			TermSuggestion termSuggestion = autocompleteList.get(i);
			JsonOntologyTermIdentifier jsonId = new JsonOntologyTermIdentifier(ontologyName, termSuggestion.getIdentifier());
			result[i] = new JsonTermSuggestion(termSuggestion.getLabel(), jsonId , termSuggestion.getDescription(), termSuggestion.getSynonyms());
		}
		return result;
	}
	
//	/**
//	 * Merge two list by inserting it after the corresponding element in the
//	 * target list.
//	 * 
//	 * @param <T>
//	 * @param target
//	 * @param insert
//	 * @return merged list
//	 */
//	static <T> List<T> mergeLists(List<T> target, List<T> insert) {
//		if (insert.isEmpty()) {
//			return target;
//		}
//		if (target.isEmpty()) {
//			target.addAll(insert);
//			insert.clear();
//			return target;
//		}
//
//		int targetLength = target.size();
//		int insertLength = insert.size();
//		if (targetLength == insertLength) {
//			target.add(insert.remove(insertLength - 1));
//			insertLength = insert.size();
//		}
//		else if (targetLength < insertLength) {
//			int fromIndex = Math.max(0, insertLength - (insertLength - targetLength + 1));
//			List<T> subList = new ArrayList<T>(insert.subList(fromIndex, insertLength));
//			target.addAll(subList);
//			insert.removeAll(subList);
//			insertLength = insert.size();
//		}
//		for (int i = insert.size() - 1; i >= 0; i--) {
//			target.add(i + 1, insert.remove(i));
//		}
//		return target;
//	}

}
