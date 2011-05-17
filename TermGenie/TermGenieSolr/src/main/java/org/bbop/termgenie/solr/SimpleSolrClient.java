package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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

public class SimpleSolrClient implements OntologyTermSuggestor {
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

	List<OntologyTerm> searchGeneOntologyTerms(String query, String branch, int maxCount) {
		CommonsHttpSolrServer server = SolrClientFactory.getServer(baseUrl);
		// escape query string of solr/lucene query syntax
		query = ClientUtils.escapeQueryChars(query);
		try {
			// simple query
			SolrDocumentList results = query(query, branch, server, 0, maxCount);

			// deal with strange sorting for short queries and many hits with
			// the same maximum score
			Float maxScore = results.getMaxScore();
			List<SolrDocument> solrDocuments = getDocumentsWithMaxScore(results, maxScore);
			if (maxCount < 100 && solrDocuments.size() == maxCount
					&& results.getNumFound() > maxCount) {
				// assume there are even more hits with max score.
				// need to fetch all (max 100), to re-rank the top hits
				SolrDocumentList result2 = query(query, branch, server, maxCount, 100 - maxCount);
				solrDocuments.addAll(getDocumentsWithMaxScore(result2, maxScore));
				
				// sort results by ascending label length
				sortbyLabelLength(solrDocuments);
				if (solrDocuments.size() > maxCount) {
					solrDocuments = solrDocuments.subList(0, maxCount);
				}
			}
			List<OntologyTerm> terms = new ArrayList<OntologyTerm>(solrDocuments.size());
			for (SolrDocument solrDocument : solrDocuments) {
				OntologyTerm term = getOntologyTerm(solrDocument);
				if (term != null) {
					terms.add(term);
				}
			}
			return terms;

		} catch (SolrServerException exception) {
			logger.warn("Problem quering solr server at: " + baseUrl, exception);
			return null;
		}
	}

	void sortbyLabelLength(List<SolrDocument> solrDocuments) {
		Collections.sort(solrDocuments, new Comparator<SolrDocument>() {

			@Override
			public int compare(SolrDocument o1, SolrDocument o2) {
				final String label1 = o1.getFieldValue("label").toString();
				final String label2 = o2.getFieldValue("label").toString();
				int l1 = label1.length();
				int l2 = label2.length();
				return (l1 < l2 ? -1 : (l1 == l2 ? 0 : 1));
			}
		});
	}

	List<SolrDocument> getDocumentsWithMaxScore(SolrDocumentList results, Float maxScore) {
		if (maxScore == null) {
			return Collections.emptyList();
		}
		List<SolrDocument> documents = new ArrayList<SolrDocument>();
		for (SolrDocument solrDocument : results) {
			Float score = (Float) solrDocument.getFieldValue("score");
			if (score != null && Math.abs(maxScore.floatValue() - score.floatValue()) <= 0.0001f) {
				documents.add(solrDocument);
			} else {
				break;
			}
		}
		return documents;
	}

	SolrDocumentList query(String query, String branch, CommonsHttpSolrServer server,
			int start, int chunkSize) throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery().
		// search for query  as literal string and as prefix
		setQuery("label:(" + query + " " + query + "*)"). 
		setRows(chunkSize). // length
		setStart(start). // offset
		setParam("version", "2.2").
		// include score in results
		setParam("fl", "*,score"). 
		// search for ontology terms
		addFilterQuery("document_category:ontology_class"); 
		if (branch != null) {
			// search only for one branch
			// (i.e. biological_process, molecular_function, cellular_component)
			solrQuery.addFilterQuery("source:" + branch);
		}
		else {
			// TODO hack the server contains also cell terms without a source info
			solrQuery.addFilterQuery("source:(biological_process OR molecular_function OR cellular_component)");
		}
		QueryResponse rsp = server.query(solrQuery);
		SolrDocumentList results = rsp.getResults();
		return results;
	}

	static OntologyTerm getOntologyTerm(SolrDocument solrDocument) {
		final String id = solrDocument.getFieldValue("id").toString();
		final String label = solrDocument.getFieldValue("label").toString();
		Object descObj = solrDocument.getFieldValue("description");
		final String def = descObj != null ? descObj.toString() : null;
		return new OntologyTerm() {

			@Override
			public String getId() {
				return id;
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
				return null;
			}

			@Override
			public String getLogicalDefinition() {
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
		System.out.println("-----------------------------");
		 List<OntologyTerm> terms2 = client.searchGeneOntologyTerms("pigm",null, 10);
		 for (OntologyTerm term : terms2) {
			 System.out.println(term);
		 }
	}
}
