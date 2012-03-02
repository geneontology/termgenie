package org.bbop.termgenie.services.review.mail;

import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultReviewMailHandler implements ReviewMailHandler {
	
	private static final Logger logger = Logger.getLogger(DefaultReviewMailHandler.class);

	private MailHandler mailHandler = null;
	private String fromAddress = null;
	
	/**
	 * @param mailHandler
	 * @param fromAddress
	 */
	@Inject
	public DefaultReviewMailHandler(MailHandler mailHandler, String fromAddress) {
		super();
		this.mailHandler = mailHandler;
		this.fromAddress = fromAddress;
	}

	@Override
	public void handleReviewMail(List<Integer> historyIds, AfterReview afterReview) {
		for (Integer historyId : historyIds) {
			try {
				CommitHistoryItem historyItem = afterReview.getItem(historyId);
				List<CommitedOntologyTerm> terms = historyItem.getTerms();
				StringBuilder body = new StringBuilder();
				final String init;
				final String subject;
				if (terms.size() > 1) {
					subject = "Your requested terms have been committed to the ontology.";
					init = "Hello,\n\nafter a review the following requested terms have been committed to ontology:\n\n";
				}
				else {
					subject = "Your requested term has been committed to the ontology.";
					init = "Hello,\n\nafter a review the following requested term has been committed to ontology:\n\n";
				}
				body.append(init);
				for (CommitedOntologyTerm commitedOntologyTerm : terms) {
					body.append(commitedOntologyTerm.getObo());
					body.append('\n');
				}
				final String email = historyItem.getEmail();
				
				try {
					mailHandler.sendEmail(subject, body.toString(), fromAddress, "TermGenie", email);
				} catch (EmailException exception) {
					logger.warn("Could not send e-mail to user: "+email, exception);
				}
			} catch (CommitException exception) {
				logger.warn("Could not fetch history item: "+historyId, exception);
			}
		}
	}

}
