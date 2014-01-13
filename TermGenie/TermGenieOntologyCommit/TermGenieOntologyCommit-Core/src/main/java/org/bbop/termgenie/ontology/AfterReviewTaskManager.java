package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;

class AfterReviewTaskManager extends GenericTaskManager<AfterReview> {

	private final AfterReview instance;
	
	/**
	 * @param name
	 * @param instance
	 */
	AfterReviewTaskManager(String name, AfterReview instance) {
		super(name);
		this.instance = instance;
	}

	@Override
	protected AfterReview createManaged() {
		return instance;
	}

	@Override
	protected AfterReview updateManaged(AfterReview managed) {
		return instance;
	}

	@Override
	protected AfterReview resetManaged(AfterReview managed) {
		return instance;
	}

	@Override
	protected void setChanged(boolean reset) {
		// do nothing
	}

	@Override
	protected void dispose(AfterReview managed) {
		// do nothing
	}
}