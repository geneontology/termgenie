package org.bbop.termgenie.index;

import static org.bbop.termgenie.index.AutoCompletionTools.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Basic auto-completion using an in-memory lucene index.
 */
public class LuceneMemoryOntologyIndex implements Closeable {

	private static final Logger logger = Logger.getLogger(LuceneMemoryOntologyIndex.class);
	
	private static final Version version = Version.LUCENE_33;
	private static final String DEFAULT_FIELD = "label";
	private static final String BRANCH_FIELD = "branch";
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
	 * @param roots
	 * @param branches
	 * @throws IOException
	 */
	public LuceneMemoryOntologyIndex(OWLGraphWrapper ontology, List<String> roots, List<Pair<String,List<String>>> branches) throws IOException {
		super();
		if (logger.isInfoEnabled()) {
			StringBuilder message = new StringBuilder();
			message.append("Start creating lucene memory index for: ");
			message.append(ontology.getOntologyId());
			if (roots != null) {
				message.append(" Root: ");
				message.append(roots);
			}
			if (branches != null && !branches.isEmpty()) {
				message.append(" Branches: ");
				for(Pair<String, List<String>> branch : branches) {
					message.append(" (");
					message.append(branch.getOne());
					message.append(",");
					message.append(branch.getTwo());
					message.append(") ");
				}
			}
			logger.info(message.toString());	
		}
		this.ontology = ontology;
		
		Map<String, Analyzer> alternatives = new HashMap<String, Analyzer>();
		WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(version);
		alternatives.put(ID_FIELD, whitespaceAnalyzer);
		if (branches != null && !branches.isEmpty()) {
			alternatives.put(BRANCH_FIELD, whitespaceAnalyzer);
		}
		analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(version), alternatives);
		
		RAMDirectory directory = new RAMDirectory();
		IndexWriterConfig conf = new IndexWriterConfig(version, analyzer);
		IndexWriter writer = new IndexWriter(directory, conf);
		Set<OWLObject> allOWLObjects;
		ReasonerTaskManager taskManager = getReasonerManager(ontology);
		if (roots == null || roots.isEmpty()) {
			allOWLObjects = this.ontology.getAllOWLObjects();
		}
		else {
			allOWLObjects = getDecendantsReflexive(roots, taskManager);
		}
		
		BranchInfos branchInfos = null;
		if (branches != null) {
			branchInfos = new BranchInfos();
			for (Pair<String, List<String>> branch : branches) {
				String name = branch.getOne();
				List<String> ids = branch.getTwo();
				if (ids != null && !ids.isEmpty()) {
					branchInfos.add(name, getDecendantsReflexive(ids, taskManager));
				}
			}
			branchInfos.setup();
		}
		int npeCounter = 0;
		int obsoleteCounter = 0;
		
		for (OWLObject owlObject : allOWLObjects) {
			boolean isObsolete = this.ontology.getIsObsolete(owlObject);
			if (isObsolete) {
				obsoleteCounter += 1;
				continue;
			}
			String value = this.ontology.getLabel(owlObject);
			if (value != null) {
				Document doc  = new Document();
				doc.add(new Field(DEFAULT_FIELD, value, Store.NO, Index.ANALYZED));
				try {
					String identifier = this.ontology.getIdentifier(owlObject);
					doc.add(new Field(ID_FIELD, identifier, Store.YES, Index.NOT_ANALYZED));
					if (branchInfos != null && branchInfos.isValid()) {
						for(String branchName : branchInfos.getBranches(owlObject)) {
							doc.add(new Field(BRANCH_FIELD, branchName, Store.NO,
									Index.NOT_ANALYZED));
						}
					}
					writer.addDocument(doc);
				} catch (NullPointerException exception) {
					npeCounter += 1;
					logger.error("NPE for getting an ID for: "+owlObject);
				}
			}
						
		}
		
		writer.optimize();
		writer.close();
		
		searcher = new IndexSearcher(directory);
		if (logger.isInfoEnabled()) {
			logger.info("Finished creating index for: "+ontology.getOntologyId());
			if (branchInfos != null && branchInfos.isValid()) {
				logger.info(branchInfos.createSummary());
			}
			if (obsoleteCounter > 0) {
				logger.info("Skipped "+obsoleteCounter+" obsolete terms during index creation");
			}
			if (npeCounter > 0) {
				logger.info("During the index creation there were "+npeCounter+" NPEs");
			}
		}
	}

	protected ReasonerTaskManager getReasonerManager(OWLGraphWrapper ontology) {
		return ReasonerFactory.getDefaultTaskManager(ontology);
	}
	
	private class BranchInfos {
		List<String> names = new ArrayList<String>();
		List<Set<OWLObject>> objects = new ArrayList<Set<OWLObject>>();
		int[] objectsCounts = null;
		
		void add(String name, Set<OWLObject> objects) {
			if (objects != null && !objects.isEmpty()) {
				names.add(name);
				this.objects.add(objects);
				logger.info("Found branch " + name + " with " + 
						objects.size() + " terms.");
			}
		}
		
		void setup() {
			int length = names.size();
			objectsCounts = new int[length];
			for (int i = 0; i < length; i++) {
				objectsCounts[i] = 0;
			}
		}
		
		boolean isValid() {
			return !names.isEmpty() && objects.size() == names.size()
			 && objectsCounts != null && objectsCounts.length == objects.size();
		}
		
		List<String> getBranches(OWLObject x) {
			List<String> branches = new ArrayList<String>(3);
			for (int i = 0; i < names.size(); i++) {
				String branchName = names.get(i);
				Set<OWLObject> objects = this.objects.get(i);
				if (objects.contains(x)) {
					branches.add(branchName);
					objectsCounts[i] += 1;
				}
			}
			
			return Collections.emptyList();
		}
		
		String createSummary() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < objectsCounts.length; i++) {
				if (sb.length() > 0) {
					sb.append("; ");
				}
				sb.append("Inserted terms for branch ");
				sb.append(names.get(i));
				sb.append(": ");
				sb.append(objectsCounts[i]);
			}
			return sb.toString();
		}
		
	}
	
	private Set<OWLObject> getDecendantsReflexive(List<String> ids, ReasonerTaskManager taskManager) {
		Set<OWLObject> result = new HashSet<OWLObject>();
		for(String id : ids) {
			OWLObject x = this.ontology.getOWLObjectByIdentifier(id);
			if (x == null) {
				throw new RuntimeException("Error: could not find term with id: "+id);
			}
			result.add(x);
			Collection<OWLObject> owlObjects = taskManager.getDescendants(x, this.ontology);
			if (owlObjects != null && !owlObjects.isEmpty()) {
				for (OWLObject owlObject : owlObjects) {
					if (!owlObject.isBottomEntity() && !owlObject.isTopEntity()) {
						result.add(owlObject);
					}	
				}
				
			}
		}
		return result;
	}
	
	public Collection<SearchResult> search(String queryString, int maxCount, String branch) {
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
			queryString = tools.preprocessQuery(queryString, ID_FIELD);
			if (queryString == null) {
				// do not search for strings with no tokens
				return Collections.emptyList();
			}
			
			QueryParser p = new QueryParser(version, DEFAULT_FIELD, analyzer);
			if (branch != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(BRANCH_FIELD);
				sb.append(":(");
				sb.append(branch);
				sb.append(") AND (");
				sb.append(queryString);
				sb.append(")");
				queryString = sb.toString();
			}
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

	public static void main(String[] args) {
		
		Injector injector = Guice.createInjector(new DefaultOntologyModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		ConfiguredOntology go = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontology = injector.getInstance(OntologyLoader.class).getOntology(go);

		ontology.runManagedTask(new OntologyTask(){

			@Override
			public boolean run(OWLGraphWrapper managed) {
				try {
					LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(managed, null, null);
					Collection<SearchResult> results = index.search(" me  pigmentation ", 5, null);
					for (SearchResult searchResult : results) {
						String id = managed.getIdentifier(searchResult.hit);
						String label = managed.getLabel(searchResult.hit);
						System.out.println(id+"  "+searchResult.score+"  "+label);	
					}
					return false;
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}				
			}
			
		});
	}

	@Override
	public void close() {
		try {
			searcher.close();
		} catch (IOException exception) {
			logger.warn("Could not close lucene searcher.", exception);
		}
		analyzer.close();
	}
}
