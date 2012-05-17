package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bushe.swing.event.EventBus;

import owltools.graph.OWLGraphWrapper;

public abstract class OntologyTaskManager extends GenericTaskManager<OWLGraphWrapper> {

	/**
	 * A task which requires an ontology.
	 */
	public static abstract class OntologyTask implements ManagedTask<OWLGraphWrapper> {

		private final Modified modified;
		private Exception exception = null;
		private String message = null;

		public static class TaskException extends Exception {
			
			// generated
			private static final long serialVersionUID = -7605045356162398095L;
			
			private final Modified modified;
			private final Exception exception;
			
			/**
			 * @param exception
			 * @param modified
			 */
			public TaskException(Exception exception, Modified modified)
			{
				super();
				this.exception = exception;
				this.modified = modified;
			}
		}
		
		protected OntologyTask() {
			this(Modified.no);
		}
		
		protected OntologyTask(Modified modified) {
			this.modified = modified;
		}
		
		@Override
		public final Modified run(OWLGraphWrapper managed) {
			try {
				if (modified != null) {
					runCatching(managed);
				}
				return runCatchingMod(managed);
			} catch (TaskException exception) {
				this.exception = exception.exception;
				return exception.modified;
			} catch (Exception exception) {
				this.exception = exception;
			}
			return Modified.no;
		}
		
		/**
		 * @param managed
		 * @throws TaskException
		 * @throws Exception
		 */
		protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
			// overwrite this method to do actual work,
			// the returned modification is fixed in the constructor
		}
		
		/**
		 * @param managed
		 * @return
		 * @throws TaskException
		 * @throws Exception
		 */
		protected Modified runCatchingMod(OWLGraphWrapper managed) throws TaskException, Exception {
			return Modified.no;
		}

		/**
		 * @return the exception
		 */
		public Exception getException() {
			return exception;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}
		
		protected void setMessage(String message) {
			this.message = message;
		}
	}

	protected final Ontology ontology;
	private String ontologyId = null;

	public OntologyTaskManager(Ontology ontology) throws InvalidManagedInstanceException {
		super("OntologyTaskManager-" + ontology.getUniqueName());
		this.ontology = ontology;
		runManagedTask(new OntologyTask() {

			@Override
			protected void runCatching(OWLGraphWrapper managed) {
				ontologyId = managed.getOntologyId();
			}
		});
	}

	public String getOntologyId() {
		return ontologyId;
	}

	@Override
	protected void setChanged(boolean reset) {
		EventBus.publish(new OntologyChangeEvent(this, reset));
	}

	@Override
	protected OWLGraphWrapper resetManaged(OWLGraphWrapper managed) throws InstanceCreationException {
		return createManaged();
	}

	public Ontology getOntology() {
		return ontology;
	}

}
