package org.bbop.termgenie.core.rules;

import java.util.concurrent.Semaphore;

/**
 * Provide basic runtime management for an instance. 
 * Allow only one concurrent usage of the managed instance.
 *
 * @param <T> type of the managed instance
 */
public abstract class GenericTaskManager<T> {

	private volatile T managed = null;
	private final Semaphore lock = new Semaphore(1, true); // binary and fair

	private T getManaged() {
		try {
			lock.acquire();
			if (managed == null) {
				managed = createManaged();
			}
			return managed;
		} catch (InterruptedException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private void returnManaged(T reasoner) {
		lock.release();
	}
	
	/**
	 * Create a managed instance.
	 * 
	 * @return managed instance
	 */
	protected abstract T createManaged();
	
	/**
	 * Update the current managed instance. 
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 */
	protected abstract T updateManaged(T managed);
	
	/**
	 * Tell the reasoner to update its knowledge base. Wait 
	 * until the reasoning of the other processes is finished.
	 */
	public void updateManaged() {
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			if (managed == null) {
				managed = createManaged();
			}
			else {
				managed = updateManaged(managed);
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
	 * Run a managed task. Encapsulate the wait and return operations for the managed instance.
	 * 
	 * @param task
	 */
	public void runManagedTask(ManagedTask<T> task) {
		T managed = null;
		try {
			managed = getManaged();
			task.run(managed);
		}
		finally {
			if (managed != null) {
				returnManaged(managed);
			}
		}
	}

	/**
	 * A task which requires a managed instance. 
	 */
	public static interface ManagedTask<T> {
		
		/**
		 * Run the task with a managed instance.
		 * 
		 * @param managed
		 */
		public void run(T managed);
	}
}
