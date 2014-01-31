package org.bbop.termgenie.mail.review;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import com.google.inject.Singleton;

@Singleton
public class NoopReviewMailHandler implements ReviewMailHandler {
	
	private static final Logger logger = Logger.getLogger(NoopReviewMailHandler.class);
	private static boolean ENABLE_LOGGING = false;

	@Override
	public void handleReviewMail(CommitHistoryItem item, NameProvider nameProvider) {
		// do nothing
		if (ENABLE_LOGGING && logger.isInfoEnabled()) {
			List<CommitedOntologyTerm> terms = item.getTerms();
			StringBuilder sb = new StringBuilder();
			for (CommitedOntologyTerm term : terms) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(term.getId());
			}
			logger.info("Fake post review e-mail for "+sb);
		}
	}

	@Override
	public void handleSubmitMail(CommitHistoryItem item, CommitResult commitResult, NameProvider nameProvider) {
		// do nothing
		if (ENABLE_LOGGING && logger.isInfoEnabled()) {
			List<CommitedOntologyTerm> terms = item.getTerms();
			StringBuilder sb = new StringBuilder();
			for (CommitedOntologyTerm term : terms) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(term.getId());
			}
			logger.info("Fake post submit e-mail for "+sb);
		}
	}

	public static class NoopReviewMailHandlerModule extends IOCModule {

		public NoopReviewMailHandlerModule(Properties applicationProperties) {
			super(applicationProperties);
		}

		@Override
		protected void configure() {
			bind(ReviewMailHandler.class, NoopReviewMailHandler.class);
		}
		
	}
}
