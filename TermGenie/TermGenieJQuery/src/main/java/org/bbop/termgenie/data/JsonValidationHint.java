package org.bbop.termgenie.data;

import org.bbop.termgenie.data.JsonTermTemplate.JsonTemplateField;


public class JsonValidationHint {

	public static final int FATAL = 15;
	public static final int ERROR = 5;
	public static final int WARN = 5;

	private JsonTemplateField field;
	private int level;
	private String hint;

	/**
	 * Default constructor required for serialization.
	 */
	private JsonValidationHint() {
		super();
	}

	/**
	 * @param field
	 * @param level
	 * @param hint
	 */
	public JsonValidationHint(JsonTemplateField field, int level, String hint) {
		this();
		this.field = field;
		this.level = level;
		this.hint = hint;
	}

	/**
	 * @param field
	 * @param hint
	 */
	public JsonValidationHint(JsonTemplateField field, String hint) {
		this(field, ERROR, hint);
	}

	/**
	 * @return the field
	 */
	public JsonTemplateField getField() {
		return field;
	}

	/**
	 * @param field
	 *            the field to set
	 */
	public void setField(JsonTemplateField field) {
		this.field = field;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * @param hint
	 *            the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}
}