package org.bbop.termgenie.owl;

import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;

public interface RelationshipTask extends ReasonerTask {

	/**
	 * @return the {@link InferredRelations}
	 */
	public InferredRelations getInferredRelations();

}
