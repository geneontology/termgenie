package org.bbop.termgenie.ontology;


/**
 * Specialized exception, which can be thrown druring the commit on an ontology change.
 */
public class CommitException extends Exception {

	// generated
	private static final long serialVersionUID = 3325142257733756945L;

	/**
	 * Create a new exception, with an error message.
	 * 
	 * @param message
	 */
	public CommitException(String message) {
		super(message);
	}
	
	/**
	 * Create a new exception, with an error message and a thrown exception as cause
	 * 
	 * @param message
	 * @param exception cause
	 */
	public CommitException(String message, Throwable exception) {
		super(message, exception);
	}
}
