package org.bbop.termgenie.index;

import static org.bbop.termgenie.index.AutoCompletionTools.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

/**
 * Basic auto-completion using an in-memory lucene index.
 */
public class LuceneMemoryOntologyIndex {
	
	private static final Logger logger = Logger.getLogger(LuceneMemoryOntologyIndex.class);
	
	private static final Version version = Version.LUCENE_31;
	private static final String DEFAULT_FIELD = "label";
	private static final String ID_FIELD = "id";
	private static final FieldSelector FIELD_SELECTOR = new FieldSelector() {
		
		private static final long serialVersionUID = 139300915748750525L;

		@Override
		public FieldSelectorResult accept(String fieldName) {
			// only load the id field
			if (ID_FIELD.equals(fieldName)) {
				return FieldSelectorResult.LOAD;
			}
			return FieldSelectorResult.NO_LOAD;
		}
	};
	
	private final AutoCompletionTools<SearchResult> tools = new AutoCompletionTools<SearchResult>() {
		
		@Override
		protected String getLabel(SearchResult t) {
			return ontology.getLabel(t.hit);
		}
		
		@Override
		protected String escape(String string) {
			return QueryParser.escape(string);
		}
	};
	
	private final OWLGraphWrapper ontology;
	private final Analyzer analyzer;
	private final IndexSearcher searcher;
	
	/**
	 * @param ontology
	 */
	public LuceneMemoryOntologyIndex(OWLGraphWrapper ontology) throws IOException {
		super();
		this.ontology = ontology;
		analyzer = new StandardAnalyzer(version);
		
		RAMDirectory directory = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig(version, analyzer);
		IndexWriter writer = new IndexWriter(directory, conf);
		Set<OWLObject> allOWLObjects = ontology.getAllOWLObjects();
		
		for (OWLObject owlObject : allOWLObjects) {
			Document doc  = new Document();
			String value = ontology.getLabel(owlObject);
			if (value != null) {
				Field field1 = new Field(DEFAULT_FIELD, value, Store.NO, Index.ANALYZED);
				Field field2 = new Field(ID_FIELD, ontology.getIdentifier(owlObject), Store.YES,
						Index.NOT_ANALYZED);
				doc.add(field1);
				doc.add(field2);
			}
			writer.addDocument(doc);			
		}
		
		writer.optimize();
		writer.close();
		
		searcher = new IndexSearcher(directory);
	}
	
	public Collection<SearchResult> search(String queryString, int maxCount) {
		try {
			if (queryString == null || queryString.isEmpty() || maxCount < 1) {
				// no empty string search
				// max count minimum value: 1
				return Collections.emptyList();
			}
			queryString = queryString.trim();
			if (queryString.length() <= 1) {
				// do not search for strings with only one char
				return Collections.emptyList();
			}
			queryString = tools.preprocessQuery(queryString);
			
			QueryParser p = new QueryParser(version, DEFAULT_FIELD, analyzer);
			Query query = p.parse(queryString);
			TopDocs topDocs = searcher.search(query, maxCount);
			if (topDocs.totalHits == 0) {
				return Collections.emptyList();
			}
			
			boolean rerank = false;
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			float maxScore = topDocs.getMaxScore();
			if (maxCount < 100 && scoreDocs.length == maxCount) {
				float lastScore = scoreDocs[scoreDocs.length - 1].score;
				if (fEquals(maxScore, lastScore)) {
					rerank = true;
					maxScore = topDocs.getMaxScore();
					topDocs = searcher.search(query, 100);
					scoreDocs = topDocs.scoreDocs;
				}
			}
			
			List<SearchResult> results = new ArrayList<SearchResult>(scoreDocs.length);
			
			for (ScoreDoc scoreDoc : scoreDocs) {
				Document doc = searcher.doc(scoreDoc.doc, FIELD_SELECTOR);
				String id = doc.get("id");
				OWLObject owlObject = ontology.getOWLObjectByIdentifier(id);
				if (owlObject != null) {
					if (!rerank || fEquals(maxScore, scoreDoc.score) ) {
						results.add(new SearchResult(owlObject, scoreDoc.score));
					}
				}
			}
			if (rerank) {
				tools.sortbyLabelLength(results);
			}
			if (results.size() > maxCount) {
				results = results.subList(0, maxCount);
			}
			return results;
		} catch (Exception exception) {
			logger.warn("Could not execute search", exception);
		}
		return Collections.emptyList();
	}
	
	public static class SearchResult {
		
		public final OWLObject hit;
		public final float score;
		
		/**
		 * @param hit
		 * @param score
		 */
		SearchResult(OWLObject hit, float score) {
			super();
			this.hit = hit;
			this.score = score;
		}
	}

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
		
		
		DefaultOntologyLoader loader = new DefaultOntologyLoader();
		ConfiguredOntology go = DefaultOntologyConfiguration.getOntologies().get("GeneOntology");
		
		OWLGraphWrapper ontology = loader.getOntology(go);

		LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(ontology);
		Collection<SearchResult> results = index.search(" me  pigmentation ", 5);
		for (SearchResult searchResult : results) {
			String id = ontology.getIdentifier(searchResult.hit);
			String label = ontology.getLabel(searchResult.hit);
			System.out.println(id+"  "+searchResult.score+"  "+label);	
		}
	}
}
