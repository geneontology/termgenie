package org.bbop.termgenie.core.management;

import java.util.concurrent.Semaphore;

/**
 * Provide basic runtime management for an instance. 
 * Allow limited concurrent usage of the managed instance.
 *
 * @param <T> type of the managed instance
 */
public abstract class GenericTaskManager<T> {

	private volatile T managed = null;
	private final Semaphore lock; 
	final String name;
	
	/**
	 * Create a new manager, with a binary and fair semaphore.
	 * 
	 * @param name the name of this manager
	 */
	public GenericTaskManager(String name) {
		this(name, 1); // binary and fair
	}
	
	/**
	 * Create a new manager, allowing n number of concurrent calls.
	 * Low Level, only to be used in this package
	 * 
	 * @param name the name of this manager
	 * @param n number of concurrent users
	 */
	GenericTaskManager(String name, int n) {
		this.lock = new Semaphore(n, true); // fair
		this.name = name;
	}
	/**
	 * Low level method to lock.
	 * Only to be used in this package
	 * 
	 * @return managed
	 */
	T getManaged() {
		try {
			lock.acquire();
			if (managed == null) {
				managed = createManaged();
				if (managed == null) {
					throw new GenericTaskManagerException("The managed object in manager "+name+" must never be null!");
				}
			}
			return managed;
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		}
	}
	
	/**
	 * Low level method to unlock.
	 * Only to be used in this package
	 * 
	 * @param managed
	 * @param modified
	 */
	void returnManaged(T managed, boolean modified) {
		if (this.managed != managed) {
			throw new GenericTaskManagerException("Trying to return the wrong managed object for manager: "+name);
		}
		try {
			if (modified) {
				this.managed = resetManaged(managed);
			}
		} catch (GenericTaskManagerException exception) {
			throw exception;
		} finally {
			lock.release();
			if (modified) {
				setChanged();
			}
		}
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
	 * Update the current managed instance. 
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 */
	protected abstract T resetManaged(T managed);
	
	/**
	 * Tell the managed object to update. Wait 
	 * until the other processes are finished.
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
				setChanged();
			}
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.",exception);
		}
		finally {
			if (hasLock) {
				lock.release();
			}
		}
	}
	
	protected abstract void setChanged();

	/**
	 * Run a managed task. Encapsulate the wait and return operations for the managed instance.
	 * 
	 * @param task
	 */
	public void runManagedTask(ManagedTask<T> task) {
		T managed = null;
		boolean modified = false;
		try {
			managed = getManaged();
			modified = task.run(managed);
		}
		finally {
			if (managed != null) {
				returnManaged(managed, modified);
			}
		}
	}

	/**
	 * A task which requires a managed instance.
	 *  
	 * @param <T> 
	 */
	public static interface ManagedTask<T> {
		
		/**
		 * Run the task with a managed instance.
		 * 
		 * @param managed
		 * @return true if the instance was modified
		 */
		public boolean run(T managed);
	}
	
	public static class GenericTaskManagerException extends RuntimeException {

		// generated
		private static final long serialVersionUID = -204418633281300080L;

		public GenericTaskManagerException(String message) {
			super(message);
		}
		
		public GenericTaskManagerException(String message, Throwable exception) {
			super(message, exception);
		}
	}
}
