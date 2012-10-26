package org.bbop.termgenie.services.freeform;

public class JsonFreeFormHint {

	private String field;
	private String hint;

	/**
	 * Default constructor required for serialization.
	 */
	public JsonFreeFormHint() {
		super();
	}

	/**
	 * @param field
	 * @param hint
	 */
	public JsonFreeFormHint(String field, String hint) {
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
	public void setField(int String) {
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
