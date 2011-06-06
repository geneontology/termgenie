package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;

public class LuceneOnlyClient implements OntologyTermSuggestor {
	
	private final Map<String, BasicLuceneClient> luceneIndices;

	public LuceneOnlyClient(Collection<? extends Ontology> ontologies) {
		super();
		luceneIndices = createIndices(ontologies);
	}
	
	private static Map<String, BasicLuceneClient> createIndices(Collection<? extends Ontology> ontologies) {
		
		Map<String, List<Ontology>> groups = new HashMap<String, List<Ontology>>();
		for (Ontology ontology : ontologies) {
			String name = ontology.getUniqueName();
			List<Ontology> group = groups.get(name);
			if (group == null) {
				group = new ArrayList<Ontology>();
				groups.put(name, group);
			}
			group.add(ontology);
		}

		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (String name : groups.keySet()) {
			indices.put(name, BasicLuceneClient.create(groups.get(name)));
		}
		return indices;
	}
	
	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		BasicLuceneClient index = luceneIndices.get(ontology.getUniqueName());
		if (index != null) {
			return index.suggestTerms(query, ontology, maxCount);
		}
		return null;
	}
}
