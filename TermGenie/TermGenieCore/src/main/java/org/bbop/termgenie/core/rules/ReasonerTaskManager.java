package org.bbop.termgenie.core.rules;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

public abstract class ReasonerTaskManager extends GenericTaskManager<OWLReasoner> {

	/**
	 * A task which requires a reasoner. 
	 */
	public static interface ReasonerTask extends ManagedTask<OWLReasoner>{}
}
