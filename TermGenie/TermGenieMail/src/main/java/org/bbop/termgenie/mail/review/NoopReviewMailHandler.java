package org.bbop.termgenie.mail.review;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;

import com.google.inject.Singleton;

@Singleton
public class NoopReviewMailHandler implements ReviewMailHandler {

	@Override
	public void handleReviewMail(CommitHistoryItem item) {
		// do nothing
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
