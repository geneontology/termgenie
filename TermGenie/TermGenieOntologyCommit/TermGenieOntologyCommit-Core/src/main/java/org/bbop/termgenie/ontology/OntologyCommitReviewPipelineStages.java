package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;

/**
 * 
 */
public interface OntologyCommitReviewPipelineStages {

	public Committer getReviewCommitter();

	public BeforeReview getBeforeReview();

	public GenericTaskManager<AfterReview> getAfterReview();

	public interface BeforeReview {

		public List<CommitHistoryItem> getItemsForReview() throws CommitException;
	}

	public interface AfterReview {
		
		public CommitHistoryItem getItem(String id) throws CommitException;
		
		public void updateItem(CommitHistoryItem item) throws CommitException;

		public List<CommitResult> commit(List<Integer> historyIds) throws CommitException;
	}

}
