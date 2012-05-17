package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
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

	private final String name;
	private final List<String> roots;
	private final String dlQuery;
	private final List<BranchDetails> branches;
	private final ReasonerFactory factory;
	private OntologyTaskManager ontologyManager;

	private LuceneMemoryOntologyIndex index;
	

	/**
	 * Create a new instance of an {@link OntologyTermSuggestor} using a lucene
	 * memory index.
	 * 
	 * @param ontologyManager
	 * @param factory
	 * @return new Instance of {@link BasicLuceneClient}
	 */
	public static BasicLuceneClient create(OntologyTaskManager ontologyManager, ReasonerFactory factory) {
		Ontology ontology = ontologyManager.getOntology();
		List<String> roots = ontology.getRoots();
		List<BranchDetails> branches = Collections.emptyList();
		String branchName = ontology.getBranch();
		String dlQuery = ontology.getDLQuery();
		if (branchName != null && (roots != null || dlQuery != null)) {
			BranchDetails detail = new BranchDetails(branchName, roots, dlQuery);
			branches = Collections.singletonList(detail);
			roots = null;
		}
		String name = ontology.getUniqueName();
		return create(ontologyManager, name, roots, dlQuery, branches, factory);
	}

	/**
	 * @param ontologies
	 * @param manager
	 * @param factory
	 * @return new Instance of {@link BasicLuceneClient}
	 */
	public static BasicLuceneClient create(List<Ontology> ontologies,
			OntologyTaskManager manager,
			ReasonerFactory factory)
	{
		if (ontologies == null || ontologies.isEmpty()) {
			throw new RuntimeException("At least one ontology is required to create an index.");
		}
		if (ontologies.size() == 1) {
			return create(manager, factory);
		}
		List<String> roots = null;
		String name = null;
		List<BranchDetails> branches = new ArrayList<BranchDetails>();
		String dlQuery = null;
		for (Ontology ontology : ontologies) {
			if (name == null) {
				name = ontology.getUniqueName();
				dlQuery = ontology.getDLQuery();
			}
			else {
				String cname = ontology.getUniqueName();
				if (!name.equals(cname)) {
					throw new RuntimeException("Error: Expected only one ontology group, but was: " + name + " and " + cname);
				}
			}
			String branchName = ontology.getBranch();
			List<String> cRoots = ontology.getRoots();
			String branchDLQuery = ontology.getDLQuery();
			if (branchName == null) {
				roots = cRoots;
				dlQuery = branchDLQuery;
			}
			else if (cRoots != null || branchDLQuery != null) {
				BranchDetails detail = new BranchDetails(branchName, cRoots, branchDLQuery);
				branches.add(detail);
			}
		}
		return create(manager, name, roots, dlQuery, branches, factory);
	}

	private static BasicLuceneClient create(OntologyTaskManager ontology,
			String name,
			List<String> roots,
			String dlQuery,
			List<BranchDetails> branches,
			ReasonerFactory factory)
	{
		final BasicLuceneClient client = new BasicLuceneClient(name, roots, dlQuery, branches, ontology, factory);
		client.setup();
		return client;
	}

	/**
	 * @param name
	 * @param roots
	 * @param dlQuery
	 * @param branches
	 * @param ontologyManager
	 * @param factory
	 */
	protected BasicLuceneClient(String name,
			List<String> roots,
			String dlQuery,
			List<BranchDetails> branches,
			OntologyTaskManager ontologyManager,
			ReasonerFactory factory)
	{
		super();
		this.name = name;
		this.roots = roots;
		this.dlQuery = dlQuery;
		this.branches = branches;
		this.ontologyManager = ontologyManager;
		this.factory = factory;
		EventBus.subscribe(SecondaryOntologyChangeEvent.class, this);
	}

	void setup() {
		LuceneMemoryOntologyIndex old = index;
		try {
			OntologyTask task = new OntologyTask() {

				@Override
				protected void runCatching(OWLGraphWrapper graph) throws TaskException, Exception {
					index = new LuceneMemoryOntologyIndex(graph, roots, dlQuery, branches, factory);
				}
			};
			ontologyManager.runManagedTask(task);
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
			// check if the changed ontology is the one used for this index
			if (this.name.equals(event.getOntology().getUniqueName())) {
				this.ontologyManager = event.getManager();
				setup();
			}
		}
	}

	@Override
	public List<TermSuggestion> suggestTerms(String query, Ontology ontology, int maxCount) {
		if (this.name.equals(ontology.getUniqueName())) {
			Collection<SearchResult> searchResults = index.search(query,
					maxCount,
					ontology.getBranch());
			if (searchResults != null && !searchResults.isEmpty()) {
				final List<TermSuggestion> suggestions = new ArrayList<TermSuggestion>(searchResults.size());
				for (SearchResult searchResult : searchResults) {
					suggestions.add(searchResult.term);
				}
				return suggestions;
			}
		}
		return null;
	}

}
