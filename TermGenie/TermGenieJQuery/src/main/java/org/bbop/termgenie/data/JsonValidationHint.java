package org.bbop.termgenie.data;

public class JsonValidationHint {

	public static final int FATAL = 15;
	public static final int ERROR = 10;
	public static final int WARN = 5;

	private JsonTermTemplate template;
	private int field;
	private int level;
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
	 * @param level
	 * @param hint
	 */
	public JsonValidationHint(JsonTermTemplate template, int field, int level, String hint) {
		this();
		this.template = template;
		this.field = field;
		this.level = level;
		this.hint = hint;
	}

	/**
	 * @param template
	 * @param field
	 * @param hint
	 */
	public JsonValidationHint(JsonTermTemplate template, int field, String hint) {
		this(template, field, ERROR, hint);
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
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
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
	 * @param hint the hint to set
	 */
	public void setHint(String hint) {
		this.hint = hint;
	}
}