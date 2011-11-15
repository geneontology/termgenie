package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.tools.OntologyTools;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OntologyServiceImpl implements OntologyService {

	private final OntologyTools ontologyTools;
	private final OntologyTermSuggestor suggestor;
	private final MultiOntologyTaskManager multiOntologyTaskManager;

	/**
	 * @param ontologyTools
	 * @param suggestor
	 * @param multiOntologyTaskManager
	 */
	@Inject
	OntologyServiceImpl(OntologyTools ontologyTools, OntologyTermSuggestor suggestor, MultiOntologyTaskManager multiOntologyTaskManager) {
		super();
		this.ontologyTools = ontologyTools;
		this.suggestor = suggestor;
		this.multiOntologyTaskManager = multiOntologyTaskManager;
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
			List<String> autocompleteList = suggestor.suggestTerms(query, ontology, max);
			if (autocompleteList == null || autocompleteList.isEmpty()) {
				// no terms found, do nothing
				continue;
			}
			// prepare suggestions
			CreateJsonTermSuggestionTask task = new CreateJsonTermSuggestionTask(autocompleteList, ontologyTools.getOntologyName(ontology));
			multiOntologyTaskManager.runManagedTask(task, ontology);
			mergeLists(suggestions, task.result);
		}
		if (suggestions.size() > max) {
			suggestions = suggestions.subList(0, max);
		}
		return suggestions.toArray(new JsonTermSuggestion[suggestions.size()]);
	}
	
	private static class CreateJsonTermSuggestionTask  implements MultiResourceManagedTask<OWLGraphWrapper, Ontology> {

		private final String ontologyName;
		private final List<String> ids; 
		List<JsonTermSuggestion> result = new ArrayList<JsonTermSuggestion>();
		
		
		public CreateJsonTermSuggestionTask(List<String> ids, String ontologyName) {
			this.ids = ids;
			this.ontologyName = ontologyName;
		}
		
		@Override
		public List<Modified> run(List<OWLGraphWrapper> requested) {
			OWLGraphWrapper wrapper = requested.get(0);
			for(String id : ids) {
				JsonOntologyTermIdentifier identifier = new JsonOntologyTermIdentifier(ontologyName, id);
				OWLClass owlClass = wrapper.getOWLClassByIdentifier(id);
				String label = wrapper.getLabel(owlClass);
				String def = wrapper.getDef(owlClass);
				List<String> synonyms = getSynonyms(wrapper.getOBOSynonyms(owlClass));
				result.add(new JsonTermSuggestion(label, identifier, def, synonyms));
			}
			return null;
		}
		
		private List<String> getSynonyms(List<ISynonym> synonyms) {
			if (synonyms != null && !synonyms.isEmpty()) {
				List<String> strings = new ArrayList<String>(synonyms.size());
				for (ISynonym synonym : synonyms) {
					strings.add(synonym.getLabel());
				}
				return strings;
			}
			return null;
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
