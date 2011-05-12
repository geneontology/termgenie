package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
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
		// default server
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
		// escape query string of solr/lucene query syntax
		query = ClientUtils.escapeQueryChars(query);
		try {
			SolrDocumentList results = query(query, branch, server, 0, maxCount);
//			Float maxScore = results.getMaxScore();
			List<OntologyTerm> terms = new ArrayList<OntologyTerm>(results.size());
			for (SolrDocument solrDocument : results) {
//				Float score = (Float) solrDocument.getFieldValue("score");
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
	
	private SolrDocumentList query(String query, String branch, CommonsHttpSolrServer server, int start, int chunkSize) throws SolrServerException {
		SolrQuery solrQuery = new  SolrQuery().
        setQuery("label:("+query+" "+query+"*)"). // search for query as literal string and as prefix
        setRows(chunkSize). // length
        setStart(start). // offset
        setParam("version", "2.2").
//        setParam("sort","score desc, label asc").
        setParam("fl", "*,score"). // include score in results
		addFilterQuery("document_category:ontology_class"); // search for ontology terms
		if (branch != null) {
			// search only for one branch 
			// (biological_process,molecular_function,cellular_component)
			solrQuery.addFilterQuery("source:" + branch);
		}
		QueryResponse rsp = server.query(solrQuery);
		SolrDocumentList results = rsp.getResults();
		return results;
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
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		SimpleSolrClient client = new SimpleSolrClient();
		List<OntologyTerm> terms = client.searchGeneOntologyTerms("pig", "biological_process", 10);
		for (OntologyTerm term : terms) {
			System.out.println(term);
		}
		// tried to reproduce the timeout or connection closed exceptions
//		List<OntologyTerm> terms2 = client.searchGeneOntologyTerms("pigm", "biological_process", 10);
//		for (OntologyTerm term : terms2) {
//			System.out.println(term);
//		}
//		Thread.sleep(60*1000L);
//		List<OntologyTerm> terms3 = client.searchGeneOntologyTerms("pigment", "biological_process", 10);
//		for (OntologyTerm term : terms3) {
//			System.out.println(term);
//		}
	}
}
