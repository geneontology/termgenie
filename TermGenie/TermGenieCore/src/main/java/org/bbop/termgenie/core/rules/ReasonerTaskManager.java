package org.bbop.termgenie.core.rules;

import java.util.concurrent.Semaphore;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

public abstract class ReasonerTaskManager {

	private OWLReasoner reasoner = null;
	private final Semaphore lock = new Semaphore(1, true); // binary and fair

	private OWLReasoner getReasoner() {
		try {
			lock.acquire();
			if (reasoner == null) {
				reasoner = createReasoner();
			}
			return reasoner;
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private void returnReasoner(OWLReasoner reasoner) {
		lock.release();
	}
	
	/**
	 * Create a reasoner instance.
	 * 
	 * @return reasoner instance
	 */
	protected abstract OWLReasoner createReasoner();
	
	/**
	 * Update the current reasoner. 
	 * 
	 * @param reasoner current reasoner
	 * @return update reasoner
	 */
	protected abstract OWLReasoner updateReasoner(OWLReasoner reasoner);
	
	/**
	 * Tell the reasoner to update its knowledge base. Wait 
	 * until the reasoning of the other processes is finished.
	 */
	public void updateReasoner() {
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			if (reasoner == null) {
				reasoner = createReasoner();
			}
			else {
				reasoner = updateReasoner(reasoner);
			}
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			if (hasLock) {
				lock.release();
			}
		}
	}
	
	/**
	 * Run a reasoner task. Encapsulate the wait and return operations for the reasoner.
	 * 
	 * @param task
	 */
	public void runReasonerTask(ReasonerTask task) {
		OWLReasoner reasoner = null;
		try {
			reasoner = getReasoner();
			task.reason(reasoner);
		}
		finally {
			if (reasoner != null) {
				returnReasoner(reasoner);
			}
		}
	}

	/**
	 * A task which requires a reasoner. 
	 */
	public static interface ReasonerTask {
		
		/**
		 * Run the task with a reasoner.
		 * 
		 * @param reasoner
		 */
		public void reason(OWLReasoner reasoner);
	}
}
