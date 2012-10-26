package org.bbop.termgenie.freeform;

/**
 * Individual remark for an hint or error during a free form request validation.
 */
public class FreeFormHint {

	private String field;
	private String hint;

	/**
	 * Default constructor required for serialization.
	 */
	public FreeFormHint() {
		super();
	}

	/**
	 * @param field
	 * @param hint
	 */
	public FreeFormHint(String field, String hint) {
		this.field = field;
		this.hint = hint;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * @param hint the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}

}
