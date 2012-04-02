package org.bbop.termgenie.core.process;

/**
 * Bean for representing an message during the process execution.
 */
public class ProcessStateMessage {

	private String time;
	private String message;
	private String details;

	/**
	 * Default constructor, required for RPC.
	 */
	public ProcessStateMessage() {
		super();
		this.time = null;
		this.message = null;
		this.details = null;
	}
	
	/**
	 * @param time
	 * @param message
	 * @param details
	 */
	public ProcessStateMessage(String time, String message, String details) {
		super();
		this.time = time;
		this.message = message;
		this.details = details;
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}
}