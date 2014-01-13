package org.bbop.termgenie.ontology.owl;

import java.util.List;

import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.semanticweb.owlapi.model.OWLOntology;


public class DefaultOwlTermFilter implements TermFilter<OWLOntology> {

	@Override
	public List<CommitedOntologyTerm> filterTerms(CommitHistoryItem item,
			OWLOntology targetOntology,
			List<OWLOntology> allOntologies,
			int position)
	{
		if (position == 0) {
			return item.getTerms();
		}
		return null;
	}

}
