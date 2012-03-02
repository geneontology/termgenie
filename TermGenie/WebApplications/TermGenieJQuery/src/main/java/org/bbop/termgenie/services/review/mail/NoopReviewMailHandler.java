package org.bbop.termgenie.services.review.mail;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;

import com.google.inject.Singleton;

@Singleton
public class NoopReviewMailHandler implements ReviewMailHandler {

	@Override
	public void handleReviewMail(List<Integer> historyIds, AfterReview afterReview) {
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
