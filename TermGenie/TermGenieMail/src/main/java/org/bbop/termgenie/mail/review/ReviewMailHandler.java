package org.bbop.termgenie.mail.review;

import org.bbop.termgenie.ontology.entities.CommitHistoryItem;


public interface ReviewMailHandler {

	public void handleReviewMail(CommitHistoryItem item);
}
