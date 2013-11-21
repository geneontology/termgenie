package org.bbop.termgenie.core;

import java.util.List;

public interface OntologyTermSuggestor {

	public List<TermSuggestion> suggestTerms(String query, String subset, int maxCount);
}
