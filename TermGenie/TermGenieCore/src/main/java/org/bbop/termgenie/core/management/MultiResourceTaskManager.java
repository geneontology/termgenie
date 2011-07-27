package org.bbop.termgenie.core.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.bbop.termgenie.core.management.GenericTaskManager.GenericTaskManagerException;

/**
 * Enable locking of multiple resources for one task. Prevent deadlocks 
 * by allowing only one task at a time to acquire multiple locks.
 * This is only deadlock free if all processes acquire their locks at 
 * the beginning of the task and need no further managed resources 
 * during execution.
 *
 * @param <RESOURCETYPE> type of the managed instances
 * @param <INFOTYPE> additional info for a resource
 */
public abstract class MultiResourceTaskManager<RESOURCETYPE, INFOTYPE> {
	
	private final GenericTaskManager<RESOURCETYPE>[] managers;
	private final INFOTYPE[] infos;
	private final Semaphore lock; 
	private final String name;
	
	protected MultiResourceTaskManager(String name, GenericTaskManager<RESOURCETYPE>...managers) {
		super();
		this.name = name;
		this.managers = managers;
		this.infos = getAdditionalInformations(managers);
		this.lock = new Semaphore(1, true); // binary and fair
		// binary is required to avoid deadlocks
	}
	
	protected abstract INFOTYPE[] getAdditionalInformations(GenericTaskManager<RESOURCETYPE>...managers);
	
	/**
	 * Check if the info types match. Used to filter requested resources.
	 * 
	 * @param i1
	 * @param i2
	 * @return boolean
	 */
	protected abstract boolean matchRequested(INFOTYPE i1, INFOTYPE i2);
	
	private GenericTaskManager<RESOURCETYPE> getResource(INFOTYPE requested) {
		for (int i = 0; i < infos.length; i++) {
			if (matchRequested(requested, infos[i])) {
				return managers[i];
			}
		}
		return null;
	}
	
	private List<RESOURCETYPE> getManaged(INFOTYPE[] infos) {
		List<RESOURCETYPE> resources = new ArrayList<RESOURCETYPE>(infos.length);
		boolean hasLock = false;
		try {
			lock.acquire();
			hasLock = true;
			for (INFOTYPE requested : infos) {
				GenericTaskManager<RESOURCETYPE> manager = getResource(requested);
				resources.add(manager.getManaged());
			}
			return resources;
		} catch (InterruptedException exception) {
			throw new GenericTaskManagerException("Interrupted during wait in "+MultiResourceTaskManager.class.getSimpleName()+": "+name, exception);
		} catch (GenericTaskManagerException exception) {
			// release all allocated resources so far in case of error
			List<GenericTaskManagerException> innerExceptions = new ArrayList<GenericTaskManagerException>();
			for (int i=0; i<resources.size(); i++) {
				try {
					managers[i].returnManaged(resources.get(i), false);
				} catch (GenericTaskManagerException innerException) {
					// log the exception, but continue releasing
					innerExceptions.add(innerException);
				}
			}
			if (innerExceptions.isEmpty()) {
				throw exception;
			}
			throw new MultipleTaskManagerExceptionsException(name, exception, innerExceptions);
		}
		finally {
			if (hasLock) {
				lock.release();
			}
		}
	}
	
	private void returnManaged(List<RESOURCETYPE> resources, List<Boolean> modifieds, INFOTYPE[] infos) {
		//no locking required for releasing
		if (infos.length != resources.size()) {
			throw new GenericTaskManagerException("Trying to return a resource list incompatible (different length) with its request");
		}
		List<GenericTaskManagerException> exceptions = new ArrayList<GenericTaskManagerException>();
		for (int i = 0; i < infos.length; i++) {
			try {
				GenericTaskManager<RESOURCETYPE> manager = getResource(infos[i]);
				boolean modified = false;
				if (modifieds != null && modifieds.size() > i) {
					modified = modifieds.get(i);
				}
				manager.returnManaged(resources.get(i), modified);
			} catch (GenericTaskManagerException exception) {
				exceptions.add(exception);
			}
		}
		if (!exceptions.isEmpty()) {
			GenericTaskManagerException first = exceptions.get(0);
			int size = exceptions.size();
			if (size == 1) {
				throw first;
			}
			throw new MultipleTaskManagerExceptionsException(name, first, exceptions.subList(1, size));
		}
	}
	
	/**
	 * Run a managed task. Encapsulate the wait and return 
	 * operations for the managed instances.
	 * 
	 * @param task
	 * @param requested
	 */
	public void runManagedTask(MultiResourceManagedTask<RESOURCETYPE, INFOTYPE> task, INFOTYPE...requested) {
		List<RESOURCETYPE> managed = null;
		List<Boolean> modifiedList = null;
		try {
			managed = getManaged(requested);
			modifiedList = task.run(managed);
		}
		finally {
			if (managed != null) {
				returnManaged(managed, modifiedList, requested);
			}
		}
	}
	
	/**
	 * A task which requires multiple managed resources. 
	 */
	public static interface MultiResourceManagedTask<RESOURCETYPE, INFOTYPE> {
		
		/**
		 * Run the task with a managed instance of each resource.
		 * 
		 * @param managed
		 */
		public List<Boolean> run(List<RESOURCETYPE> requested);
		
	}
	
	public static class MultipleTaskManagerExceptionsException extends GenericTaskManagerException {

		// generated
		private static final long serialVersionUID = 5678784738501530229L;
		
		private final Exception initialException;
		private final Exception[] furtherExceptions;
		
		MultipleTaskManagerExceptionsException(String name,
				GenericTaskManagerException exception, 
				List<GenericTaskManagerException> innerExceptions) {
			super("Additional errors "+innerExceptions.size()+" in "+name+" during handling of the exception: "+exception.getMessage(), exception);
			this.initialException = exception;
			this.furtherExceptions = innerExceptions.toArray(new Exception[innerExceptions.size()]);
		}

		/**
		 * @return the initialException
		 */
		public Exception getInitialException() {
			return initialException;
		}

		/**
		 * @return the furtherExceptions
		 */
		public Exception[] getFurtherExceptions() {
			return furtherExceptions;
		}
	}
}
