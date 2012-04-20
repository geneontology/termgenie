package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;


public interface TermFilter<ONTOLOGY> {

	/**
	 * Filter the list of terms and terms for the required/appropriate changes.
	 * 
	 * @param item
	 * @param targetOntology
	 * @param allOntologies
	 * @param position
	 * @return list of terms to be modified
	 */
	public List<CommitedOntologyTerm> filterTerms(CommitHistoryItem item,
			ONTOLOGY targetOntology,
			List<ONTOLOGY> allOntologies, 
			int position);
}
