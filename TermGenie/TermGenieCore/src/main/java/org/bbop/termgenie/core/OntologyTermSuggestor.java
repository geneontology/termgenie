package org.bbop.termgenie.core;

import java.util.List;

public interface OntologyTermSuggestor {

	public List<String> suggestTerms(String query, Ontology ontology, int maxCount);
}
