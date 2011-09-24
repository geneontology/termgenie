package org.bbop.termgenie.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.tools.Pair;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public class BasicLuceneClient implements
		OntologyTermSuggestor,
		EventSubscriber<OntologyChangeEvent>
{

	private final String name;
	private final List<String> roots;
	private final List<Pair<String, List<String>>> branches;
	private final ReasonerFactory factory;

	private LuceneMemoryOntologyIndex index;
	private OWLGraphWrapper ontology;

	/**
	 * Create a new instance of an {@link OntologyTermSuggestor} using a lucene
	 * memory index.
	 * 
	 * @param ontology
	 * @param factory
	 * @return new Instance of {@link BasicLuceneClient}
	 */
	public static BasicLuceneClient create(OntologyTaskManager ontology, ReasonerFactory factory) {
		List<String> roots = ontology.getOntology().getRoots();
		List<Pair<String, List<String>>> branches = Collections.emptyList();
		String branchName = ontology.getOntology().getBranch();
		if (branchName != null & roots != null) {
			Pair<String, List<String>> pair = new Pair<String, List<String>>(branchName, roots);
			branches = Collections.singletonList(pair);
			roots = null;
		}
		String name = ontology.getOntology().getUniqueName();
		return create(ontology, name, roots, branches, factory);
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
		List<Pair<String, List<String>>> branches = new ArrayList<Pair<String, List<String>>>();
		for (Ontology ontology : ontologies) {
			if (name == null) {
				name = ontology.getUniqueName();
			}
			else {
				String cname = ontology.getUniqueName();
				if (!name.equals(cname)) {
					throw new RuntimeException("Error: Expected only one ontology group, but was: " + name + " and " + cname);
				}
			}
			String branchName = ontology.getBranch();
			List<String> cRoots = ontology.getRoots();
			if (branchName == null) {
				roots = cRoots;
			}
			else if (cRoots != null) {
				Pair<String, List<String>> pair = new Pair<String, List<String>>(branchName, cRoots);
				branches.add(pair);
			}
		}
		return create(manager, name, roots, branches, factory);
	}

	private static BasicLuceneClient create(OntologyTaskManager ontology,
			String name,
			List<String> roots,
			List<Pair<String, List<String>>> branches,
			ReasonerFactory factory)
	{
		BasicLuceneClient client = new BasicLuceneClient(name, roots, branches, factory);
		LuceneClientSetupTask task = new LuceneClientSetupTask(client);
		ontology.runManagedTask(task);
		return task.client;
	}

	static class LuceneClientSetupTask implements OntologyTask {

		BasicLuceneClient client = null;

		/**
		 * @param client
		 */
		LuceneClientSetupTask(BasicLuceneClient client) {
			super();
			this.client = client;
		}

		@Override
		public Modified run(OWLGraphWrapper managed) {
			client.setup(managed);
			return Modified.no;
		}

	}

	/**
	 * @param name
	 * @param roots
	 * @param branches
	 */
	protected BasicLuceneClient(String name,
			List<String> roots,
			List<Pair<String, List<String>>> branches,
			ReasonerFactory factory)
	{
		super();
		this.name = name;
		this.roots = roots;
		this.branches = branches;
		this.factory = factory;
		EventBus.subscribe(OntologyChangeEvent.class, this);
	}

	void setup(OWLGraphWrapper ontology) {
		this.ontology = ontology;
		LuceneMemoryOntologyIndex old = index;
		try {
			index = new LuceneMemoryOntologyIndex(ontology, roots, branches, factory);
			if (old != null) {
				old.close();
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void onEvent(OntologyChangeEvent event) {
		// ignore event, if it's just a reset
		if (!event.isReset()) {
			// check if the changed ontology is the one used for this index
			if (this.name.equals(event.getOntology().getUniqueName())) {
				LuceneClientSetupTask task = new LuceneClientSetupTask(this);
				event.getManager().runManagedTask(task);
			}
		}
	}

	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		if (this.name.equals(ontology.getUniqueName())) {
			Collection<SearchResult> searchResults = index.search(query,
					maxCount,
					ontology.getBranch());
			if (searchResults != null && !searchResults.isEmpty()) {
				List<OntologyTerm> suggestions = new ArrayList<OntologyTerm>(searchResults.size());
				for (SearchResult searchResult : searchResults) {
					suggestions.add(createTerm(searchResult.hit));
				}
				return suggestions;
			}
		}
		return null;
	}

	private OntologyTerm createTerm(OWLObject hit) {
		final String identifier = ontology.getIdentifier(hit);
		final String label = ontology.getLabel(hit);
		final String def = ontology.getDef(hit);
		List<Synonym> synonyms = ontology.getOBOSynonyms(hit);
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(identifier, label, def, synonyms, null, Collections.<String, String> emptyMap(), null);
		return term;
	}

}
