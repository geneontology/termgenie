package org.bbop.termgenie.ontology.obo;

import java.util.List;

import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.obolibrary.oboformat.model.OBODoc;

public final class DefaultOboTermFilter implements TermFilter<OBODoc> {

	@Override
	public List<CommitedOntologyTerm> filterTerms(CommitHistoryItem item,
			OBODoc targetOntology,
			List<OBODoc> allOntologies, 
			int position)
	{
		if (position == 0) {
			return item.getTerms();
		}
		return null;
	}
}