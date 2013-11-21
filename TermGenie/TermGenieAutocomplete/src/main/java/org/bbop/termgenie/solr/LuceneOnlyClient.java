package org.bbop.termgenie.solr;

import java.util.List;

import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.ontology.OntologyTaskManager;

/**
 * Term suggestor using only an in-memory lucene index.
 */
public class LuceneOnlyClient implements OntologyTermSuggestor {

	private final BasicLuceneClient index;

	/**
	 * @param manager 
	 * @param factory
	 */
	public LuceneOnlyClient(OntologyTaskManager manager, ReasonerFactory factory)
	{
		super();
		index = BasicLuceneClient.create(manager, factory);
	}

	@Override
	public List<TermSuggestion> suggestTerms(String query, String subset, int maxCount) {
		return index.suggestTerms(query, subset, maxCount);
	}
}
