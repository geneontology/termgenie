package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.AbstractOntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.index.AutoCompletionTools;

import owltools.graph.OWLGraphWrapper.Synonym;

public class SimpleSolrClient implements OntologyTermSuggestor {

	private static final Logger logger = Logger.getLogger(SimpleSolrClient.class);
	private final String baseUrl;

	// package private for testing purposes
	static final AutoCompletionTools<SolrDocument> solrTools = new AutoCompletionTools<SolrDocument>()
	{

		@Override
		protected String getLabel(SolrDocument t) {
			return t.getFieldValue("label").toString();
		}

		@Override
		protected String escape(String string) {
			return ClientUtils.escapeQueryChars(string);
		}
	};

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

	@Override
	public List<OntologyTerm<Synonym, IRelation>> suggestTerms(String query, Ontology ontology, int maxCount) {
		if ("GeneOntology".equals(ontology.getUniqueName())) {
			return searchGeneOntologyTerms(query, ontology.getBranch(), maxCount);
		}
		return null;
	}

	protected List<OntologyTerm<Synonym, IRelation>> searchGeneOntologyTerms(String query, String branch, int maxCount)
	{
		CommonsHttpSolrServer server = SolrClientFactory.getServer(baseUrl);
		// escape query string of solr/lucene query syntax and create query
		query = solrTools.preprocessQuery(query, null);
		try {
			// simple query
			SolrDocumentList results = query(query, branch, server, 0, maxCount);

			// deal with strange sorting for short queries and many hits with
			// the same maximum score
			Float maxScore = results.getMaxScore();
			List<SolrDocument> solrDocuments = getDocumentsWithMaxScore(results, maxScore);
			if (maxCount < 100 && solrDocuments.size() == maxCount && results.getNumFound() > maxCount) {
				// assume there are even more hits with max score.
				// need to fetch all (max 100), to re-rank the top hits
				SolrDocumentList result2 = query(query, branch, server, maxCount, 100 - maxCount);
				solrDocuments.addAll(getDocumentsWithMaxScore(result2, maxScore));

				// sort results by ascending label length
				solrTools.sortbyLabelLength(solrDocuments);
				if (solrDocuments.size() > maxCount) {
					solrDocuments = solrDocuments.subList(0, maxCount);
				}
			}
			List<OntologyTerm<Synonym, IRelation>> terms = new ArrayList<OntologyTerm<Synonym, IRelation>>(solrDocuments.size());
			for (SolrDocument solrDocument : solrDocuments) {
				OntologyTerm<Synonym, IRelation> term = getOntologyTerm(solrDocument);
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

	List<SolrDocument> getDocumentsWithMaxScore(SolrDocumentList results, Float maxScore) {
		if (maxScore == null) {
			return Collections.emptyList();
		}
		List<SolrDocument> documents = new ArrayList<SolrDocument>();
		for (SolrDocument solrDocument : results) {
			Float score = (Float) solrDocument.getFieldValue("score");
			if (score != null && Math.abs(maxScore.floatValue() - score.floatValue()) <= 0.0001f) {
				documents.add(solrDocument);
			}
			else {
				break;
			}
		}
		return documents;
	}

	SolrDocumentList query(String query,
			String branch,
			CommonsHttpSolrServer server,
			int start,
			int chunkSize) throws SolrServerException
	{
		SolrQuery solrQuery = new SolrQuery().
		// search for query as literal string and as prefix
		setQuery(query). // query
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
			// TODO hack the server contains also cell terms without a source
			// info
			solrQuery.addFilterQuery("source:(biological_process OR molecular_function OR cellular_component)");
		}
		QueryResponse rsp = server.query(solrQuery);
		SolrDocumentList results = rsp.getResults();
		return results;
	}

	static OntologyTerm<Synonym, IRelation> getOntologyTerm(SolrDocument solrDocument) {
		final String id = solrDocument.getFieldValue("id").toString();
		final String label = solrDocument.getFieldValue("label").toString();
		Object descObj = solrDocument.getFieldValue("description");
		final String def = descObj != null ? descObj.toString() : null;
		return new DefaultOntologyTerm(id, label, def, null, null, Collections.<String, String> emptyMap(), null);
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		SimpleSolrClient client = new SimpleSolrClient();
		List<OntologyTerm<Synonym, IRelation>> terms = client.searchGeneOntologyTerms("pig", "biological_process", 10);
		for (OntologyTerm<Synonym, IRelation> term : terms) {
			System.out.println(term);
		}
		System.out.println("-----------------------------");
		List<OntologyTerm<Synonym, IRelation>> terms2 = client.searchGeneOntologyTerms("pigm", null, 10);
		for (OntologyTerm<Synonym, IRelation> term : terms2) {
			System.out.println(term);
		}
	}
}
