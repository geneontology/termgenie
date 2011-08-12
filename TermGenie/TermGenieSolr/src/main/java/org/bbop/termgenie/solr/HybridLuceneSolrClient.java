package org.bbop.termgenie.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.ontology.OntologyTaskManager;

public class HybridLuceneSolrClient extends SimpleSolrClient {

	private final Map<String, BasicLuceneClient> luceneIndices;

	/**
	 * @param ontologies
	 * @param factory
	 */
	public HybridLuceneSolrClient(Collection<OntologyTaskManager> ontologies,
			ReasonerFactory factory)
	{
		super();
		luceneIndices = createIndices(ontologies, factory);
	}

	/**
	 * @param baseUrl for solr index
	 * @param ontologies
	 * @param factory
	 */
	public HybridLuceneSolrClient(String baseUrl,
			Collection<OntologyTaskManager> ontologies,
			ReasonerFactory factory)
	{
		super(baseUrl);
		luceneIndices = createIndices(ontologies, factory);
	}

	private static Map<String, BasicLuceneClient> createIndices(Collection<OntologyTaskManager> ontologies,
			ReasonerFactory factory)
	{
		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (OntologyTaskManager ontology : ontologies) {
			String name = ontology.getOntology().getUniqueName();
			if ("GeneOntology".equals(name)) {
				continue;
			}
			BasicLuceneClient luceneClient = indices.get(name);
			if (luceneClient == null) {
				luceneClient = BasicLuceneClient.create(ontology, factory);
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
