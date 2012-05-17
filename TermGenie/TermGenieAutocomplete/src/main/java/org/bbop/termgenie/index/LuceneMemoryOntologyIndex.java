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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Basic auto-completion using an in-memory lucene index.
 */
public class LuceneMemoryOntologyIndex implements Closeable {

	private static final Logger logger = Logger.getLogger(LuceneMemoryOntologyIndex.class);

	private static final Version version = Version.LUCENE_33;
	private static final String LABEL_FIELD = "label";
	private static final String SYNOYM_FIELD_EXACT = "synonyms_exact";
	private static final String SYNOYM_FIELD_NARROW = "synonyms_narrow";
	private static final String SYNOYM_FIELD_RELATED = "synonyms_related";
	private static final String SYNOYM_FIELD_BROAD = "synonyms_broad";
	private static final String DESCRIPTION_FIELD = "description";
	private static final String BRANCH_FIELD = "branch";
	private static final String ID_FIELD = "id";
	private static final String LENGTH_FIELD = "length";
	
	private static final FieldSelector FIELD_SELECTOR = new FieldSelector() {

		private static final long serialVersionUID = 139300915748750525L;

		@Override
		public FieldSelectorResult accept(String fieldName) {
			// only load the id and length field
			if (BRANCH_FIELD.equals(fieldName)) {
				return FieldSelectorResult.NO_LOAD;
			}
			return FieldSelectorResult.LOAD;
		}
	};

	private final Analyzer analyzer;
	private final IndexSearcher searcher;

	public static class BranchDetails {
		
		private String name;
		private List<String> roots;
		private String dlQuery;
		
		/**
		 * @param name
		 * @param roots
		 * @param dlQuery
		 */
		public BranchDetails(String name, List<String> roots, String dlQuery) {
			super();
			this.name = name;
			this.roots = roots;
			this.dlQuery = dlQuery;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the roots
		 */
		public List<String> getRoots() {
			return roots;
		}
		
		/**
		 * @param roots the roots to set
		 */
		public void setRoots(List<String> roots) {
			this.roots = roots;
		}

		/**
		 * @return the dlQuery
		 */
		public String getDlQuery() {
			return dlQuery;
		}

		/**
		 * @param dlQuery the dlQuery to set
		 */
		public void setDlQuery(String dlQuery) {
			this.dlQuery = dlQuery;
		}
	}
	
	/**
	 * @param ontology
	 * @param roots
	 * @param dlQuery
	 * @param branches
	 * @param reasonerFactory
	 * @throws IOException
	 */
	public LuceneMemoryOntologyIndex(OWLGraphWrapper ontology,
			List<String> roots,
			String dlQuery,
			List<BranchDetails> branches,
			ReasonerFactory reasonerFactory) throws IOException
	{
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
				for (BranchDetails branch : branches) {
					message.append(" (");
					message.append(branch.getName());
					if (branch.getRoots() != null) {
						message.append(",");
						message.append(branch.getRoots());
					}
					else if (branch.getDlQuery() != null) {
						message.append(",");
						message.append(branch.getDlQuery());
					}
					message.append(") ");
				}
			}
			logger.info(message.toString());
		}

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
		ReasonerTaskManager taskManager = getReasonerManager(ontology, reasonerFactory);
		if (dlQuery != null) {
			allOWLObjects = taskManager.executeDLQuery(dlQuery, ontology);
		}
		else {
			if (roots == null || roots.isEmpty()) {
				allOWLObjects = ontology.getAllOWLObjects();
			}
			else {
				allOWLObjects = getDecendantsReflexive(roots, ontology, taskManager);
			}
		}
		BranchInfos branchInfos = null;
		if (branches != null) {
			branchInfos = new BranchInfos();
			for (BranchDetails branch : branches) {
				String name = replaceWhitespaces(branch.getName());
				String branchDLQuery = branch.getDlQuery();
				List<String> ids = branch.getRoots();
				if (ids != null && !ids.isEmpty()) {
					branchInfos.add(name, getDecendantsReflexive(ids, ontology, taskManager));
				}
				else if (branchDLQuery != null) {
					branchInfos.add(name, taskManager.executeDLQuery(branchDLQuery, ontology));
				}
			}
			branchInfos.setup();
		}
		int npeCounter = 0;
		int obsoleteCounter = 0;

		for (OWLObject owlObject : allOWLObjects) {
			boolean isObsolete = ontology.getIsObsolete(owlObject);
			if (isObsolete) {
				obsoleteCounter += 1;
				continue;
			}
			String value = ontology.getLabel(owlObject);
			if (value != null) {
				Document doc = new Document();
				doc.add(new Field(LABEL_FIELD, value, Store.YES, Index.ANALYZED));
				doc.add(new Field(LENGTH_FIELD, Integer.toString(value.length()), Store.YES, Index.NO));
				
				List<ISynonym> synonyms = ontology.getOBOSynonyms(owlObject);
				if (synonyms != null && !synonyms.isEmpty()) {
					for (ISynonym synonym : synonyms) {
						String label = synonym.getLabel();
						String scope = synonym.getScope();
						if (label != null && label.length() > 1) {
							if (scope == null || OboFormatTag.TAG_RELATED.getTag().equals(scope)) {
								doc.add(new Field(SYNOYM_FIELD_RELATED, label, Store.YES, Index.NO));
							}
							else if (OboFormatTag.TAG_NARROW.getTag().equals(scope)) {
								doc.add(new Field(SYNOYM_FIELD_NARROW, label, Store.YES, Index.NO));
							}
							else if (OboFormatTag.TAG_EXACT.getTag().equals(scope)) {
								doc.add(new Field(SYNOYM_FIELD_EXACT, label, Store.YES, Index.NO));
							}
							else if (OboFormatTag.TAG_BROAD.getTag().equals(scope)) {
								doc.add(new Field(SYNOYM_FIELD_BROAD, label, Store.YES, Index.NO));
							}
						}
					}
				}
				String def = ontology.getDef(owlObject);
				if (def != null && def.length() > 1) {
					doc.add(new Field(DESCRIPTION_FIELD, def, Store.YES, Index.NO));
				}
				
				try {
					String identifier = ontology.getIdentifier(owlObject);
					doc.add(new Field(ID_FIELD, identifier, Store.YES, Index.NOT_ANALYZED));
					if (branchInfos != null && branchInfos.isValid()) {
						List<String> brancheNames = branchInfos.getBranches(owlObject);
						if (!brancheNames.isEmpty()) {
							for (String branchName : brancheNames) {
								doc.add(new Field(BRANCH_FIELD, branchName, Store.NO, Index.NOT_ANALYZED));
							}
						}
					}
					writer.addDocument(doc);
				} catch (NullPointerException exception) {
					npeCounter += 1;
					logger.error("NPE for getting an ID for: " + owlObject);
				}
			}

		}

		writer.close();

		searcher = new IndexSearcher(IndexReader.open(directory));
		if (logger.isInfoEnabled()) {
			logger.info("Finished creating index for: " + ontology.getOntologyId());
			if (branchInfos != null && branchInfos.isValid()) {
				logger.info(branchInfos.createSummary());
			}
			if (obsoleteCounter > 0) {
				logger.info("Skipped " + obsoleteCounter + " obsolete terms during index creation");
			}
			if (npeCounter > 0) {
				logger.info("During the index creation there were " + npeCounter + " NPEs");
			}
		}
	}

	protected ReasonerTaskManager getReasonerManager(OWLGraphWrapper ontology,
			ReasonerFactory reasonerFactory)
	{
		return reasonerFactory.getDefaultTaskManager(ontology);
	}

	private class BranchInfos {

		List<String> names = new ArrayList<String>();
		List<Set<OWLObject>> objects = new ArrayList<Set<OWLObject>>();
		int[] objectsCounts = null;

		void add(String name, Set<OWLObject> objects) {
			if (objects != null && !objects.isEmpty()) {
				names.add(name);
				this.objects.add(objects);
				logger.info("Found branch " + name + " with " + objects.size() + " terms.");
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
			return !names.isEmpty() && objects.size() == names.size() && objectsCounts != null && objectsCounts.length == objects.size();
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
			if (!branches.isEmpty()) {
				return branches;
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

	private Set<OWLObject> getDecendantsReflexive(List<String> ids, OWLGraphWrapper ontology, ReasonerTaskManager taskManager)
	{
		Set<OWLObject> result = new HashSet<OWLObject>();
		for (String id : ids) {
			OWLObject x = ontology.getOWLObjectByIdentifier(id);
			if (x == null || ontology.getLabel(x) == null) {
				throw new RuntimeException("Error: could not find term with id: " + id);
			}
			result.add(x);
			Collection<OWLObject> owlObjects = taskManager.getDescendants(x, ontology);
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
			queryString = AutoCompletionTools.preprocessQuery(queryString, ID_FIELD);
			if (queryString == null) {
				// do not search for strings with no tokens
				return Collections.emptyList();
			}

			QueryParser p = new QueryParser(version, LABEL_FIELD, analyzer);
			if (branch != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(BRANCH_FIELD);
				sb.append(":(");
				sb.append(replaceWhitespaces(branch));
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
				if (!rerank || fEquals(maxScore, scoreDoc.score)) {
					Document doc = searcher.doc(scoreDoc.doc, FIELD_SELECTOR);
					String id = doc.get(ID_FIELD);
					String label = doc.get(LABEL_FIELD);
					String lengthString = doc.get(LENGTH_FIELD);
					int length = Integer.parseInt(lengthString);
					List<String> synonyms = getSynonyms(doc);
					String description = doc.get(DESCRIPTION_FIELD);
					TermSuggestion term = new TermSuggestion(label, id, description, synonyms);
					results.add(new SearchResult(term , length, scoreDoc.score));
				}
			}
			if (rerank) {
				AutoCompletionTools.sortbyLabelLength(results);
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

	private List<String> getSynonyms(Document doc) {
		List<String> synonymList = null;
		synonymList = addSynonyms(synonymList, SYNOYM_FIELD_EXACT, doc);
		synonymList = addSynonyms(synonymList, SYNOYM_FIELD_NARROW, doc);
		synonymList = addSynonyms(synonymList, SYNOYM_FIELD_RELATED, doc);
		synonymList = addSynonyms(synonymList, SYNOYM_FIELD_BROAD, doc);
		return synonymList;
	}
	
	private List<String> addSynonyms(List<String> synonymList, String field, Document doc) {
		String[] synonyms = doc.getValues(field);
		if (synonyms != null && synonyms.length > 0) {
			if (synonymList == null) {
				synonymList = new ArrayList<String>(synonyms.length);
			}
			for (String synonym : synonyms) {
				synonymList.add(synonym);
			}
		}
		return synonymList;
	}

	static String replaceWhitespaces(String s) {
		boolean replaced = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				c = '_';
				replaced = true;
			}
			sb.append(c);
		}
		if (replaced) {
			return sb.toString();
		}
		return s;
	}
	
	public static class SearchResult {

		public final TermSuggestion term;
		public final int length;
		public final float score;

		/**
		 * @param term
		 * @param length
		 * @param score
		 */
		SearchResult(TermSuggestion term, int length, float score) {
			super();
			this.term = term;
			this.length = length;
			this.score = score;
		}
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
