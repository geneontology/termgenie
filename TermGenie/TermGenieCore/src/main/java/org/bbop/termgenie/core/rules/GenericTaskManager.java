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
	private final String name;
	
	public GenericTaskManager(String name) {
		this.name = name;
	}
	
	private T getManaged() {
		try {
			lock.acquire();
			System.err.println("Locked normal: "+name);
			if (managed == null) {
				managed = createManaged();
				if (managed == null) {
					throw new GenericTaskManagerException("The managed object of "+name+" must never be null!");
				}
			}
			return managed;
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		}
	}
	
	private void returnManaged(T reasoner) {
		lock.release();
		System.err.println("Unlocked normal: "+name);
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
			System.err.println("Locked update: "+name);
			hasLock = true;
			if (managed == null) {
				managed = createManaged();
			}
			else {
				managed = updateManaged(managed);
			}
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.",exception);
		}
		finally {
			if (hasLock) {
				lock.release();
				System.err.println("Unlocked update: "+name);
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
	
	public static class GenericTaskManagerException extends RuntimeException {
		
		public GenericTaskManagerException(String message) {
			super(message);
		}
		
		public GenericTaskManagerException(String message, Throwable exception) {
			super(message, exception);
		}
	}
}
