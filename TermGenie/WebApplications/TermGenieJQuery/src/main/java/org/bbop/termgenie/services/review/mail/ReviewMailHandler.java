package org.bbop.termgenie.services.review.mail;

import java.util.List;

import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;


public interface ReviewMailHandler {

	public void handleReviewMail(List<Integer> historyIds, AfterReview afterReview);
}
