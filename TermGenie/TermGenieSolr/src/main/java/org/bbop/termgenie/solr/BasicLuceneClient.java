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
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public class BasicLuceneClient implements OntologyTermSuggestor {

	private final LuceneMemoryOntologyIndex index;
	private final Ontology ontology; 
	
	public BasicLuceneClient(Ontology ontology) {
		this.ontology = ontology;
		try {
			index = new LuceneMemoryOntologyIndex(ontology.getRealInstance());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		if (this.ontology.getUniqueName().equals(ontology.getUniqueName())) {
			Collection<SearchResult> searchResults = index.search(query, maxCount);
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
		OWLGraphWrapper w = ontology.getRealInstance();
		final String identifier = w.getIdentifier(hit);
		final String label = w.getLabel(hit);
		final String def = w.getDef(hit);
		String[] syns = w.getSynonymStrings(hit);
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
			
		};
		return term;
	}

}
