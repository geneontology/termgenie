package org.bbop.termgenie.solr;

import java.util.List;

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
	
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		// TODO implement me
		return null;
	}	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// http://accordion.lbl.gov:8080/solr/select?qt=standard&version=2.2&rows=10&start=1&fl=*%2Cscore&q=annotation_class_label:neurogenesis&packet=10
		String url = "http://accordion.lbl.gov:8080/solr/select";
		CommonsHttpSolrServer server = SolrClientFactory.getServer(url);
		SolrQuery solrQuery = new  SolrQuery().
        setQuery("document_category:ontology_class").
        setRows(10).
        setStart(1).
        setParam("version", "2.2");
		try {
			QueryResponse rsp = server.query(solrQuery);
			SolrDocumentList results = rsp.getResults();
			for (SolrDocument solrDocument : results) {
				System.out.println(solrDocument);
			}
		} catch (SolrServerException exception) {
			throw new RuntimeException(exception);
		}
	}
}
