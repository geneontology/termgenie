package org.bbop.termgenie.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;

public class LuceneOnlyClient implements OntologyTermSuggestor {
	
	private final Map<String, BasicLuceneClient> luceneIndices;

	public LuceneOnlyClient(Collection<? extends Ontology> ontologies) {
		super();
		luceneIndices = createIndices(ontologies);
	}
	
	LuceneOnlyClient() {
		this(DefaultOntologyConfiguration.getOntologies().values());
	}

	private static Map<String, BasicLuceneClient> createIndices(Collection<? extends Ontology> ontologies) {
		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (Ontology ontology : ontologies) {
			String name = ontology.getUniqueName();
			BasicLuceneClient luceneClient = indices.get(name);
			if (luceneClient == null) {
				luceneClient = new BasicLuceneClient(ontology);
				indices.put(name, luceneClient);
			}
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
