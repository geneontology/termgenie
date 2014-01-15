package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipeline;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;

import owltools.graph.OWLGraphWrapper;

public class OboCommitReviewPipeline extends OntologyCommitReviewPipeline<OBODoc>
{
	private final TermFilter<OBODoc> termFilter;

	public OboCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			TermFilter<OBODoc> termFilter,
			ReviewMailHandler handler,
			ScmHelper<OBODoc> helper)
	{
		super(source, store, handler, helper);
		this.termFilter = termFilter;
	}

	@Override
	protected List<CommitedOntologyTerm> filterItems(CommitHistoryItem item,
			OBODoc targetOntology,
			List<OBODoc> targetOntologies,
			int i)
	{
		List<CommitedOntologyTerm> changes;
		changes = termFilter.filterTerms(item, targetOntology, targetOntologies, i);
		return changes;
	}
	
	@Override
	protected String createDiff(CommitHistoryItem historyItem, OntologyTaskManager source)
			throws CommitException
	{

		CreateDiffTask task = new CreateDiffTask(historyItem);
		try {
			source.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			throw error("Could not create diff", exception);
		}
		if (task.getException() != null) {
			throw error("Could not create diff", task.getException());
		}
		if (task.diff == null) {
			throw error("Could not create diff: empty result");
		}
		return task.diff;
	}

	private class CreateDiffTask extends OntologyTask {

		private final CommitHistoryItem historyItem;

		private String diff = null;

		public CreateDiffTask(CommitHistoryItem historyItem) {
			this.historyItem = historyItem;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			Owl2Obo owl2Obo = new Owl2Obo();
			OBODoc oboDoc = owl2Obo.convert(managed.getSourceOntology());

			List<CommitedOntologyTerm> terms = historyItem.getTerms();
			boolean succcess = applyChanges(null, terms, oboDoc);
			if (succcess) {
				List<String> ids = new ArrayList<String>(terms.size());
				for (CommitedOntologyTerm term : terms) {
					ids.add(term.getId());
				}
				diff = OboWriterTools.writeTerms(ids, oboDoc);
			}
		}
	}
}
