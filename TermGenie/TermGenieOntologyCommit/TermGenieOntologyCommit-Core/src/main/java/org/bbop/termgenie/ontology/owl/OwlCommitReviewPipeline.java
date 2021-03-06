package org.bbop.termgenie.ontology.owl;

import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipeline;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.semanticweb.owlapi.model.OWLOntology;


public class OwlCommitReviewPipeline extends OntologyCommitReviewPipeline<OWLOntology>
{

	public OwlCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			ReviewMailHandler handler,
			ScmHelper<OWLOntology> scmHelper)
	{
		super(source, store, handler, scmHelper);
	}

	@Override
	protected String createDiff(CommitHistoryItem historyItem, OntologyTaskManager source)
			throws CommitException
	{
		// TODO implement a 'simple' diff report for OWL.
		return "";
	}

}
