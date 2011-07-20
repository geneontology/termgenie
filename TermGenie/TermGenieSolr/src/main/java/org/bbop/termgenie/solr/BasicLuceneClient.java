package org.bbop.termgenie.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public class BasicLuceneClient implements OntologyTermSuggestor {

	private final LuceneMemoryOntologyIndex index;
	private final String name;
	private final OWLGraphWrapper ontology; 
	
	public static BasicLuceneClient create(OntologyTaskManager ontology) {
			List<Pair<String, String>> branches = Collections.emptyList();
			String branchName = ontology.getOntology().getBranch();
			String branchId = ontology.getOntology().getBranchId();
			if (branchName != null & branchId != null) {
				Pair<String, String> pair = new Pair<String, String>(branchName, branchId);
				branches = Collections.singletonList(pair);
			}
			String name = ontology.getOntology().getUniqueName();
			return create(ontology, name, branches);
	}
	
	public static BasicLuceneClient create(List<OntologyTaskManager> ontologies) {
		if (ontologies == null || ontologies.isEmpty()) {
			throw new RuntimeException("At least one ontology is required to create an index.");
		}
		if (ontologies.size() == 1) {
			return create(ontologies.get(0));
		}
		String name = null;
		List<Pair<String, String>> branches = new ArrayList<Pair<String,String>>();
		for (OntologyTaskManager ontology : ontologies) {
			if (name == null) {
				name = ontology.getOntology().getUniqueName();
			}
			else  {
				String cname = ontology.getOntology().getUniqueName();
				if (!name.equals(cname)) {
					throw new RuntimeException("Error: Expected only one ontology group, but was: "+name+" and "+cname);
				}
			}
			String branchName = ontology.getOntology().getBranch();
			String branchId = ontology.getOntology().getBranchId();
			if (branchName != null & branchId != null) {
				Pair<String, String> pair = new Pair<String, String>(branchName, branchId);
				branches.add(pair);
			}
		}
		return create(ontologies.get(0), name, branches);
	}
	
	private static BasicLuceneClient create(OntologyTaskManager ontology, String name, List<Pair<String, String>> branches) {
		LuceneClientCreatorTask task = new LuceneClientCreatorTask(name, branches);
		ontology.runManagedTask(task);
		return task.client;
	}
	
	static class LuceneClientCreatorTask implements OntologyTask {

		String name;
		List<Pair<String, String>> branches;
		BasicLuceneClient client = null;
		
		/**
		 * @param name
		 * @param branches
		 */
		LuceneClientCreatorTask(String name, List<Pair<String, String>> branches) {
			super();
			this.name = name;
			this.branches = branches;
		}



		@Override
		public void run(OWLGraphWrapper managed) {
			client = new BasicLuceneClient(managed, name, null, branches);
		}
		
	}
	
	protected BasicLuceneClient(OWLGraphWrapper ontology, String name, String root, List<Pair<String, String>> branches) {
		this.ontology = ontology;
		this.name = name;
		try {
			index = new LuceneMemoryOntologyIndex(ontology, root, branches);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		if (this.name.equals(ontology.getUniqueName())) {
			Collection<SearchResult> searchResults = index.search(query, maxCount, ontology.getBranch());
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
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(identifier, label, def, synonyms, null, Collections.<String, String>emptyMap(), null);
		return term;
	}

}
