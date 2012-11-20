package org.bbop.termgenie.mail.review;

import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;


public interface ReviewMailHandler {

	public void handleReviewMail(CommitHistoryItem item, NameProvider nameProvider);
	
	public void handleSubmitMail(CommitHistoryItem item, CommitResult commitResult, NameProvider nameProvider);
}
