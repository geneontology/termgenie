package org.bbop.termgenie.core;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;

public interface OntologyTermSuggestor {

	public List<OntologyTerm> suggestTerms(String query, int maxCount);
}
