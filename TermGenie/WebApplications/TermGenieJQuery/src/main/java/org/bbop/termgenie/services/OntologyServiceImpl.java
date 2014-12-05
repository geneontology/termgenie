package org.bbop.termgenie.services;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.SharedReasoner;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OntologyServiceImpl implements OntologyService {

	private final OntologyTermSuggestor suggestor;
	private final String availableOntology;
	private final OntologyLoader loader;
	private final ReasonerFactory reasonerFactory;
	private long ontologyStatusTimeout = 5L;
	private TimeUnit ontologyStatusTimeoutUnit = TimeUnit.MINUTES;

	@Inject
	OntologyServiceImpl(OntologyTermSuggestor suggestor, OntologyLoader loader, ReasonerFactory reasonerFactory) {
		super();
		this.suggestor = suggestor;
		this.loader = loader;
		this.reasonerFactory = reasonerFactory;
		this.availableOntology = loader.getOntologyManager().getOntology().getName();
	}

	@Override
	public JsonOntologyStatus getOntologyStatus() {
		final JsonOntologyStatus status = new JsonOntologyStatus();
		status.setOntology(availableOntology);
		
		OntologyTaskManager manager = loader.getOntologyManager();
		ManagedTask<OWLGraphWrapper> checkTask = new ManagedTask<OWLGraphWrapper>(){

			@Override
			public Modified run(OWLGraphWrapper graph)
			{
				SharedReasoner reasoner = reasonerFactory.getSharedReasoner(graph);
				List<String> errors = reasoner.checkConsistency(graph);
				if (errors != null && !errors.isEmpty()) {
					String[] messages = null;
					status.setOkay(false);
					messages = errors.toArray(new String[errors.size()]);
					status.setMessages(messages);
				}
				else {
					status.setOkay(true);	
				}
				return Modified.no;
			}
		};
		try {
			boolean locked = manager.runManagedTask(checkTask, ontologyStatusTimeout, ontologyStatusTimeoutUnit);
			if (locked == false) {
				status.setOkay(false);
				status.setMessages(new String[]{"Could not accquire the lock for the ontology check. The server may be busy or in an inconsitent state."});
			}
		} catch (InvalidManagedInstanceException exception) {
			status.setOkay(false);
			status.setMessages(new String[]{"The ontology is in an invalid state: "+exception.getMessage()});
		}
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
	
}
