package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologySubset;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.eventbus.SecondaryOntologyChangeEvent;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.BranchDetails;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import owltools.graph.OWLGraphWrapper;

public class BasicLuceneClient implements
		OntologyTermSuggestor,
		EventSubscriber<SecondaryOntologyChangeEvent>
{

	private final List<String> roots;
	private final String dlQuery;
	private final List<BranchDetails> branches;
	private final ReasonerFactory factory;

	private LuceneMemoryOntologyIndex index;
	

	/**
	 * Create a new instance of an {@link OntologyTermSuggestor} using a lucene
	 * memory index.
	 * 
	 * @param ontologyManager
	 * @param factory
	 */
	public BasicLuceneClient(OntologyTaskManager ontologyManager, ReasonerFactory factory)
	{
		super();
		Ontology ontology = ontologyManager.getOntology();
		this.branches = new ArrayList<BranchDetails>();
		List<OntologySubset> subsets = ontology.getSubsets();
		if (subsets != null) {
			for (OntologySubset subset : subsets) {
				branches.add(new BranchDetails(subset.getName(), subset.getRoots(), subset.getDlQuery()));
			}
		}
		this.roots = ontology.getRoots();
		this.dlQuery = ontology.getDlQuery();
		this.factory = factory;
		EventBus.subscribe(SecondaryOntologyChangeEvent.class, this);
		setup(ontologyManager);
	}

	void setup(OntologyTaskManager manager) {
		LuceneMemoryOntologyIndex old = index;
		try {
			OntologyTask task = new OntologyTask() {

				@Override
				protected void runCatching(OWLGraphWrapper graph) throws TaskException, Exception {
					index = new LuceneMemoryOntologyIndex(graph, roots, dlQuery, branches, factory);
				}
			};
			manager.runManagedTask(task);
			if (task.getException() != null) {
				throw new RuntimeException(task.getException());
			}
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		} finally {
			if (old != null) {
				old.close();
			}
		}
	}
	
	@Override
	public synchronized void onEvent(SecondaryOntologyChangeEvent event) {
		// ignore event, if it's just a reset
		if (!event.isReset()) {
			setup(event.getManager());
		}
	}

	@Override
	public List<TermSuggestion> suggestTerms(String query, String subset, int maxCount) {
		Collection<SearchResult> searchResults = index.search(query, maxCount, subset);
		if (searchResults != null && !searchResults.isEmpty()) {
			final List<TermSuggestion> suggestions = new ArrayList<TermSuggestion>(searchResults.size());
			for (SearchResult searchResult : searchResults) {
				suggestions.add(searchResult.term);
			}
			return suggestions;
		}
		return null;
	}

}
