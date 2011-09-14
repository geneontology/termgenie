package org.bbop.termgenie.data;

import java.util.Arrays;

/**
 * Result after a commit request.
 */
public class JsonCommitResult {

	private boolean success;
	private String message;
	private JsonOntologyTerm[] terms;
	private String diff;

	public JsonCommitResult() {
		super();
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

	/**
	 * @return the terms
	 */
	public JsonOntologyTerm[] getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	public void setTerms(JsonOntologyTerm[] terms) {
		this.terms = terms;
	}
	
	/**
	 * @return the diff
	 */
	public String getDiff() {
		return diff;
	}
	
	/**
	 * @param diff the diff to set
	 */
	public void setDiff(String diff) {
		this.diff = diff;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonCommitResult [success=");
		builder.append(success);
		if (message != null) {
			builder.append(", ");
			builder.append("message=");
			builder.append(message);
		}
		if (terms != null) {
			builder.append(", ");
			builder.append("terms=");
			builder.append(Arrays.toString(terms));
		}
		if (diff != null) {
			builder.append(", ");
			builder.append("diff=");
			builder.append(diff);
		}
		builder.append("]");
		return builder.toString();
	}
}