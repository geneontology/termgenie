package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;

/**
 * 
 */
public interface OntologyCommitReviewPipelineStages {
	
	public Committer getReviewCommitter();
	
	public BeforeReview getBeforeReview();
	
	public AfterReview getAfterReview();
	
	public interface BeforeReview {

		public List<CommitHistoryItem> getItemsForReview() throws CommitException;
	}

	public interface AfterReview {

		public List<CommitResult> commit(List<Integer> historyIds,
				CommitMode mode,
				String username,
				String password) throws CommitException;
	}

}
