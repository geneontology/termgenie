package org.bbop.termgenie.data;

public class JsonValidationHint {

	private JsonTermTemplate template;
	private int field;
	private String hint;

	/**
	 * Default constructor required for serialization.
	 */
	public JsonValidationHint() {
		super();
	}

	/**
	 * @param template
	 * @param field
	 * @param hint
	 */
	public JsonValidationHint(JsonTermTemplate template, int field, String hint) {
		this();
		this.template = template;
		this.field = field;
		this.hint = hint;
	}


	/**
	 * @return the template
	 */
	public JsonTermTemplate getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(JsonTermTemplate template) {
		this.template = template;
	}

	/**
	 * @return the field
	 */
	public int getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(int field) {
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
