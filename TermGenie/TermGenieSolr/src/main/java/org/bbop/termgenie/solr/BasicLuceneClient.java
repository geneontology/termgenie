package org.bbop.termgenie.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public class BasicLuceneClient implements OntologyTermSuggestor {

	private final LuceneMemoryOntologyIndex index;
	private final String name;
	private final OWLGraphWrapper ontology; 
	
	public static BasicLuceneClient create(Ontology ontology) {
			List<Pair<String, String>> branches = Collections.emptyList();
			String branchName = ontology.getBranch();
			String branchId = ontology.getBranchId();
			if (branchName != null & branchId != null) {
				Pair<String, String> pair = new Pair<String, String>(branchName, branchId);
				branches = Collections.singletonList(pair);
			}
			OWLGraphWrapper wrapper = ontology.getRealInstance();
			String name = ontology.getUniqueName();
			return new BasicLuceneClient(wrapper, name, null, branches);
	}
	
	public static BasicLuceneClient create(List<Ontology> ontologies) {
		if (ontologies == null || ontologies.isEmpty()) {
			throw new RuntimeException("At least one ontology is required to create an index.");
		}
		if (ontologies.size() == 1) {
			return create(ontologies.get(0));
		}
		String name = null;
		OWLGraphWrapper wrapper = null;
		List<Pair<String, String>> branches = new ArrayList<Pair<String,String>>();
		for (Ontology ontology : ontologies) {
			if (name == null) {
				name = ontology.getUniqueName();
				wrapper = ontology.getRealInstance();
			}
			else  {
				String cname = ontology.getUniqueName();
				if (!name.equals(cname)) {
					throw new RuntimeException("Error: Excpected only one ontology group, but was: "+name+" and "+cname);
				}
			}
			String branchName = ontology.getBranch();
			String branchId = ontology.getBranchId();
			if (branchName != null & branchId != null) {
				Pair<String, String> pair = new Pair<String, String>(branchName, branchId);
				branches.add(pair);
			}
		}
		return new BasicLuceneClient(wrapper, name, null, branches);
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
		String[] syns = ontology.getSynonymStrings(hit);
		final Set<String> synonyms;
		if (syns != null && syns.length > 0) {
			synonyms = new HashSet<String>(Arrays.asList(syns));
		}
		else {
			synonyms = Collections.emptySet();
		}
		OntologyTerm term = new OntologyTerm() {

			@Override
			public String getId() {
				return identifier;
			}

			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public String getDefinition() {
				return def;
			}

			@Override
			public Set<String> getSynonyms() {
				return synonyms;
			}

			@Override
			public String getLogicalDefinition() {
				return null;
			}

			@Override
			public List<String> getDefXRef() {
				return null;
			}

			@Override
			public String getComment() {
				return null;
			}
			
		};
		return term;
	}

}
