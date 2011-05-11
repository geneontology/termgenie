package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;

public class SimpleSolrClient implements OntologyTermSuggestor
{
	private static final Logger logger = Logger.getLogger(SimpleSolrClient.class);
	private final String baseUrl;
	
	public SimpleSolrClient() {
		this("http://accordion.lbl.gov:8080/solr/select");
	}
	
	/**
	 * @param baseUrl
	 */
	public SimpleSolrClient(String baseUrl) {
		super();
		this.baseUrl = baseUrl;
	}

	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		if ("GeneOntology".equals(ontology.getUniqueName())) {
			return searchGeneOntologyTerms(query, ontology.getBranch(), maxCount);
		}
		return null;
	}
	
	private List<OntologyTerm> searchGeneOntologyTerms(String query, String branch, int maxCount) {
		CommonsHttpSolrServer server = SolrClientFactory.getServer(baseUrl);
		SolrQuery solrQuery = new  SolrQuery().
        setQuery("label:"+query).
        setRows(10).
        setStart(0).
        setParam("version", "2.2").
		addFilterQuery("document_category:ontology_class").
		addFilterQuery("source:"+branch);
		
		try {
			QueryResponse rsp = server.query(solrQuery);
			SolrDocumentList results = rsp.getResults();
			List<OntologyTerm> terms = new ArrayList<OntologyTerm>(results.size());
			for (SolrDocument solrDocument : results) {
				OntologyTerm term = getOntologyTerm(solrDocument);
				if (term != null) {
					terms.add(term);
				}
			}
			return terms;
			
		} catch (SolrServerException exception) {
			logger.warn("Problem quering solr server at: "+baseUrl, exception);
			return null;
		}
	}
	
	private static OntologyTerm getOntologyTerm(SolrDocument solrDocument) {
		final String id = solrDocument.getFieldValue("id").toString();
		final String label = solrDocument.getFieldValue("label").toString();
		final String desc = solrDocument.getFieldValue("description").toString();
		return new OntologyTerm(){

			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public String getDescription() {
				return desc;
			}

			@Override
			public String getReferenceLink() {
				return null;
			}
		};
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleSolrClient client = new SimpleSolrClient();
		List<OntologyTerm> terms = client.searchGeneOntologyTerms("pigmentation", "biological_process", 10);
		for (OntologyTerm term : terms) {
			System.out.println(term);
		}
	}
}
