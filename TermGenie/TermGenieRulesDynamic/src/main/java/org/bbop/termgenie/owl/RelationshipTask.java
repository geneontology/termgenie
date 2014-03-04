package org.bbop.termgenie.owl;

import org.bbop.termgenie.core.rules.SharedReasoner.ReasonerTask;

public interface RelationshipTask extends ReasonerTask {

	/**
	 * @return the {@link InferredRelations}
	 */
	public InferredRelations getInferredRelations();

}
