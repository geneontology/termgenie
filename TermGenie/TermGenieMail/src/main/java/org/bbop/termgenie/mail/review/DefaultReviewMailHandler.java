package org.bbop.termgenie.mail.review;

import java.io.IOException;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.MixingNameProvider;
import org.bbop.termgenie.ontology.obo.OboParserTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DefaultReviewMailHandler implements ReviewMailHandler {

	private static final Logger logger = Logger.getLogger(DefaultReviewMailHandler.class);

	private final MailHandler mailHandler;
	private final String fromAddress;
	private final String fromName;

	/**
	 * @param mailHandler
	 * @param fromAddress
	 * @param fromName
	 */
	@Inject
	public DefaultReviewMailHandler(MailHandler mailHandler,
			@Named("DefaultReviewMailHandlerFromAddress") String fromAddress,
			@Named("DefaultReviewMailHandlerFromName") String fromName)
	{
		super();
		this.mailHandler = mailHandler;
		this.fromAddress = fromAddress;
		this.fromName = fromName;
	}

	@Override
	public void handleReviewMail(CommitHistoryItem item, NameProvider nameProvider) {
		List<CommitedOntologyTerm> terms = item.getTerms();
		StringBuilder body = new StringBuilder();
		final String init;
		final String subject;
		
		int obsoletCount = 0;
		for (CommitedOntologyTerm term : terms) {
			if (term.getObo().contains(OboFormatTag.TAG_IS_OBSELETE.getTag()+": true")) {
				obsoletCount += 1;
			}
		}
		
		if (obsoletCount == 0) {
			if (terms.size() > 1) {
				subject = "Your requested terms have been committed to the ontology.";
				init = "Hello,\n\nafter a review the following requested terms have been committed to ontology:\n\n";
			}
			else {
				subject = "Your requested term has been committed to the ontology.";
				init = "Hello,\n\nafter a review the following requested term has been committed to ontology:\n\n";
			}
		}
		else {
			if (terms.size() > 1) {
				if (terms.size() == obsoletCount) {
					subject = "Your requested terms have been obsoleted in the ontology.";
					init = "Hello,\n\nafter a review the following TermGenie requested terms have been obsoleted:\n\n";
				}
				else {
					subject = "Your requested term have either been committed  ("+
							Integer.toString(terms.size()-obsoletCount)+
							") or been obsoleted ("+
							Integer.toString(obsoletCount)+
							") in the ontology.";
					init = "Hello,\n\nafter a review the following TermGenie requested term have either been committed or been obsoleted:\n\n";
				}
			}
			else {
				subject = "Your requested term has been obsoleted in the ontology.";
				init = "Hello,\n\nafter a review the following TermGenie requested term has been obsoleted:\n\n";
			}
		}
		body.append(init);
		
		appendTerms(nameProvider, terms, body);
		final String email = item.getEmail();

		try {
			mailHandler.sendEmail(subject, body.toString(), fromAddress, fromName, email);
			logger.info("Sent e-mail to user: "+email);
		} catch (EmailException exception) {
			logger.warn("Could not send e-mail to user: " + email, exception);
		}
	}

	protected void appendTerms(NameProvider nameProvider,
			List<CommitedOntologyTerm> terms,
			StringBuilder body)
	{
		MixingNameProvider provider = new MixingNameProvider(nameProvider);
		for (CommitedOntologyTerm term : terms) {
			provider.addName(term.getId(), term.getLabel());
		}
		
		for (CommitedOntologyTerm commitedOntologyTerm : terms) {
			String obo;
			try {
				Frame frame = OboParserTools.parseFrame(commitedOntologyTerm.getId(), commitedOntologyTerm.getObo());
				obo = OboWriterTools.writeFrame(frame, provider);
			} catch (IOException exception) {
				logger.warn("Could not render OBO for e-mail, using fallback", exception);
				obo = commitedOntologyTerm.getObo();
			}
			body.append(obo);
			body.append('\n');
		}
	}

	@Override
	public void handleSubmitMail(CommitHistoryItem item, CommitResult commitResult, NameProvider nameProvider) {
		List<CommitedOntologyTerm> terms = item.getTerms();
		StringBuilder body = new StringBuilder();
		final String init;
		final String subject;
		if (terms.size() > 1) {
			subject = "Confirmation of your new term request in TermGenie.";
			init = "Hello,\n\nthis is a confirmation that the following term has been submitted for review in TermGenie:\n\n";
		}
		else {
			subject = "Confirmation of your new term requests in TermGenie.";
			init = "Hello,\n\nthis is a confirmation that the following terms have been submitted for review in TermGenie:\n\n";
		}
		body.append(init);
		appendTerms(nameProvider, terms, body);
		final String email = item.getEmail();

		try {
			mailHandler.sendEmail(subject, body.toString(), fromAddress, fromName, email);
		} catch (EmailException exception) {
			logger.warn("Could not send e-mail to user: " + email, exception);
		}
	}

}
