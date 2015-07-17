package org.bbop.termgenie.core.management;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.tools.Pair;

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
	 * Create a new manager, with a binary and fair semaphore, no timeout.
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
	 * Low level method to lock with a timeout. Only to be used in this package
	 * 
	 * @param timeout 
	 * @param timeoutUnit 
	 * @return pair of try lock status and managed instance (if available or null).
	 * @throws InvalidManagedInstanceException 
	 */
	Pair<Boolean, T> getManaged(long timeout, TimeUnit timeoutUnit) throws InvalidManagedInstanceException {

		try {
			if (timeoutUnit != null){
				boolean success = lock.tryAcquire(timeout, timeoutUnit);
				if (success == false) {
					return Pair.of(Boolean.FALSE, null);
				}
			}
			else {
				lock.acquire();

			}
			if (inValid) {
				lock.release();
				throw new InvalidManagedInstanceException("Managed instance is in an invalid state");
			}
			if (managed == null) {
				managed = createManaged();
				if (managed == null) {
					lock.release();
					throw new GenericTaskManagerException("The managed object in manager " + name + " must never be null!");
				}
			}
			return Pair.of(Boolean.TRUE, managed);
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		} catch (InstanceCreationException exception) {
			inValid = true;
			lock.release();
			throw new InvalidManagedInstanceException("Could not create managed instance: "+exception.getMessage(), exception.getCause());
		}
	}
	
	/**
	 * Low level method to lock no timeout. Only to be used in this package
	 * 
	 * @return pair of try lock status and managed instance (if available or null).
	 * @throws InvalidManagedInstanceException 
	 */
	T getManaged() throws InvalidManagedInstanceException {
		try {
			lock.acquire();
			if (inValid) {
				lock.release();
				throw new InvalidManagedInstanceException("Managed instance is in an invalid state");
			}
			if (managed == null) {
				managed = createManaged();
				if (managed == null) {
					lock.release();
					throw new GenericTaskManagerException("The managed object in manager " + name + " must never be null!");
				}
			}
			return managed;
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait.", exception);
		} catch (InstanceCreationException exception) {
			inValid = true;
			lock.release();
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
	 * @throws InstanceCreationException
	 */
	protected abstract T createManaged() throws InstanceCreationException;

	/**
	 * Update the current managed instance.
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 * @throws InstanceCreationException
	 */
	protected abstract T updateManaged(T managed) throws InstanceCreationException;

	/**
	 * Update the current managed instance.
	 * 
	 * @param managed current managed instance
	 * @return updated managed instance
	 * @throws InstanceCreationException
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

	/**
	 * Signal a change of the managed. This can be used to trigger specific change events.
	 * 
	 * @param reset set to true if the change is a pure reset
	 */
	protected abstract void setChanged(boolean reset);

	/**
	 * Run a managed task. Encapsulate the wait and return operations for the
	 * managed instance.
	 * 
	 * @param task
	 * @param timeout
	 * @param timeoutUnit 
	 * @return boolean, true if the lock was acquired
	 * @throws InvalidManagedInstanceException 
	 */
	public final boolean runManagedTask(ManagedTask<T> task, long timeout, TimeUnit timeoutUnit) throws InvalidManagedInstanceException {
		T managed = null;
		Modified modified = Modified.no;
		Pair<Boolean, T> pair = getManaged(timeout, timeoutUnit);
		if (Boolean.FALSE.equals(pair.getOne())) {
			return false;
		}
		managed = pair.getTwo();
		try {
			modified = task.run(managed);
			return true;
		}
		finally {
			if (managed != null) {
				returnManaged(managed, modified);
			}
		}
	}
	
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
		managed = getManaged();
		try {
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
			
			// also notice the change
			setChanged(false);
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
