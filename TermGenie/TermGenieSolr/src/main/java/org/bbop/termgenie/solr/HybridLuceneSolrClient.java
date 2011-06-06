package org.bbop.termgenie.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;

public class HybridLuceneSolrClient extends SimpleSolrClient {

	private final Map<String, BasicLuceneClient> luceneIndices;
	
	/**
	 * @param ontologies
	 */
	public HybridLuceneSolrClient(Collection<? extends Ontology> ontologies) {
		super();
		luceneIndices = createIndices(ontologies);
	}

	/**
	 * @param baseUrl  for solr index
	 * @param ontologies
	 */
	public HybridLuceneSolrClient(String baseUrl, Collection<? extends Ontology> ontologies) {
		super(baseUrl);
		luceneIndices = createIndices(ontologies);
	}
	
	private static Map<String, BasicLuceneClient> createIndices(Collection<? extends Ontology> ontologies) {
		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (Ontology ontology : ontologies) {
			if (ontology.getUniqueName().equals("GeneOntology")) {
				continue;
			}
			String name = ontology.getUniqueName();
			BasicLuceneClient luceneClient = indices.get(name);
			if (luceneClient == null) {
				luceneClient = BasicLuceneClient.create(ontology);
				indices.put(name, luceneClient);
			}
		}
		return indices;
	}
	
	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		if ("GeneOntology".equals(ontology.getUniqueName())) {
			return searchGeneOntologyTerms(query, ontology.getBranch(), maxCount);
		}
		BasicLuceneClient index = luceneIndices.get(ontology.getUniqueName());
		if (index != null) {
			return index.suggestTerms(query, ontology, maxCount);
		}
		return null;
	}

}
