package org.bbop.termgenie.core.management;

import java.util.concurrent.Semaphore;

import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;

/**
 * Provide basic runtime management for an instance. Allow limited concurrent
 * usage of the managed instance.
 * 
 * @param <T> type of the managed instance
 */
public abstract class GenericTaskManager<T> {

	private volatile boolean inValid = false;
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
	 * Create a new manager, allowing n number of concurrent calls. Low Level,
	 * only to be used in this package
	 * 
	 * @param name the name of this manager
	 * @param n number of concurrent users
	 */
	GenericTaskManager(String name, int n) {
		this.lock = new Semaphore(n, true); // fair
		this.name = name;
	}

	/**
	 * Low level method to lock. Only to be used in this package
	 * 
	 * @return managed
	 * @throws InvalidManagedInstanceException 
	 */
	T getManaged() throws InvalidManagedInstanceException {
		try {
			lock.acquire();
			if (inValid) {
				throw new InvalidManagedInstanceException("Managed instance is in an invalid state");
			}
			if (managed == null) {
				managed = createManaged();
				if (managed == null) {
					throw new GenericTaskManagerException("The managed object in manager " + name + " must never be null!");
				}
			}
			return managed;
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		} catch (InstanceCreationException exception) {
			inValid = true;
			throw new InvalidManagedInstanceException("Could not create managed instance: "+exception.getMessage(), exception.getCause());
		}
	}

	/**
	 * Low level method to unlock. Only to be used in this package
	 * 
	 * @param managed
	 * @param modified
	 * @throws InvalidManagedInstanceException 
	 */
	void returnManaged(T managed, Modified modified) throws InvalidManagedInstanceException {
		if (this.managed != managed) {
			throw new GenericTaskManagerException("Trying to return the wrong managed object for manager: " + name);
		}
		try {
			if (modified == Modified.reset) {
				this.managed = resetManaged(managed);
			}
			else if (modified == Modified.update) {
				this.managed = updateManaged(managed);
			}
		} catch (GenericTaskManagerException exception) {
			throw exception;
		} catch (InstanceCreationException exception) {
			inValid = true;
			throw new InvalidManagedInstanceException("Could not create managed instance: "+exception.getMessage(), exception.getCause());
		}
		finally {
			lock.release();
			if (modified == Modified.reset) {
				setChanged(true);
			}
			else if (modified == Modified.update) {
				setChanged(false);
			}
		}
	}

	/**
	 * Create a managed instance.
	 * 
	 * @return managed instance
	 */
	protected abstract T createManaged() throws InstanceCreationException;

	/**
	 * Update the current managed instance.
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 */
	protected abstract T updateManaged(T managed) throws InstanceCreationException;

	/**
	 * Update the current managed instance.
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 */
	protected abstract T resetManaged(T managed) throws InstanceCreationException;

	/**
	 * Called for disposing the managed instance.
	 * Overwrite to implement custom functionality.
	 * 
	 * @param managed
	 */
	protected abstract void dispose(T managed);
	
	/**
	 * Tell the managed object to update. Wait until the other processes are
	 * finished.
	 * 
	 * @throws InvalidManagedInstanceException 
	 */
	public final void updateManaged() throws InvalidManagedInstanceException {
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			if (inValid) {
				throw new InvalidManagedInstanceException("Managed instance is in an invalid state");
			}
//			if (inValid) {
//				managed = handleInvalid(managed);
//				
//			}
			if (managed == null) {
				managed = createManaged();
			}
			else {
				managed = updateManaged(managed);
				setChanged(false);
			}
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		} catch (InstanceCreationException exception) {
			inValid = true;
			throw new InvalidManagedInstanceException("Could not create managed instance: "+exception.getMessage(), exception.getCause());
		}
		finally {
			if (hasLock) {
				lock.release();
			}
		}
	}
	
//	/**
//	 * Allow a custom handling of invalid states of the manager.
//	 * 
//	 * @param managed
//	 * @throws InvalidManagedInstanceException
//	 */
//	protected T handleInvalid(T managed) throws InvalidManagedInstanceException {
//		throw new InvalidManagedInstanceException("Managed instance is in an invalid state");
//	}

	public final void dispose() {
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			if (managed != null) {
				dispose(managed);
				managed = null;
			}
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		}
		finally {
			if (hasLock) {
				lock.release();
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// this is a safe guard. If someone forgets to dispose an objects, 
		// which needs to be disposed, try to do it in the finalize.
		if (managed != null) {
			dispose(managed);
		}
		super.finalize();
	}

	protected abstract void setChanged(boolean reset);

	/**
	 * Run a managed task. Encapsulate the wait and return operations for the
	 * managed instance.
	 * 
	 * @param task
	 * @throws InvalidManagedInstanceException 
	 */
	public final void runManagedTask(ManagedTask<T> task) throws InvalidManagedInstanceException {
		T managed = null;
		Modified modified = Modified.no;
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
	
	public final void recoverInvalid() {
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			
			// try to silently dispose invalid instance
			T current = managed;
			managed = null;
			inValid = false;
			
			dispose(current);
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
	 * A task which requires a managed instance.
	 * 
	 * @param <T>
	 */
	public static interface ManagedTask<T> {

		public enum Modified {
			no, update, reset
		}
		
		/**
		 * Run the task with a managed instance.
		 * 
		 * @param managed
		 * @return true if the instance was modified
		 */
		public Modified run(T managed);
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
	
	public static class InstanceCreationException extends Exception {

		// generated
		private static final long serialVersionUID = 3685882108945684588L;

		public InstanceCreationException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static class InvalidManagedInstanceException extends Exception {

		// generated
		private static final long serialVersionUID = 8453914129313031825L;

		public InvalidManagedInstanceException(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public InvalidManagedInstanceException(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public InvalidManagedInstanceException(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}
		
	}
}
