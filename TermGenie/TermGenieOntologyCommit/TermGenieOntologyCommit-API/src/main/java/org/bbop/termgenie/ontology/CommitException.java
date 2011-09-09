package org.bbop.termgenie.ontology;

/**
 * Specialized exception, which can be thrown during the commit on an ontology
 * change.
 */
public class CommitException extends Exception {

	// generated
	private static final long serialVersionUID = 3325142257733756945L;

	private final boolean rollback;

	/**
	 * Create a new exception, with an error message.
	 * 
	 * @param message
	 * @param rollback
	 */
	public CommitException(String message, boolean rollback) {
		super(message);
		this.rollback = rollback;
	}

	/**
	 * Create a new exception, with an error message and a thrown exception as
	 * cause
	 * 
	 * @param message
	 * @param exception cause
	 * @param rollback
	 */
	public CommitException(String message, Throwable exception, boolean rollback) {
		super(message, exception);
		this.rollback = rollback;
	}

	/**
	 * Indicate whether the commit failed at a stage, where a rollback is
	 * possible.
	 * 
	 * @return the rollback
	 */
	public boolean isRollback() {
		return rollback;
	}
}
