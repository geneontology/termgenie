package org.bbop.termgenie.data;

public class JsonResult {

	protected boolean success;
	protected String message;

	public JsonResult() {
		// empty default constructor
	}

	/**
	 * @param success
	 * @param message
	 */
	public JsonResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * @param success
	 */
	public JsonResult(boolean success) {
		this.success = success;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
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

}
