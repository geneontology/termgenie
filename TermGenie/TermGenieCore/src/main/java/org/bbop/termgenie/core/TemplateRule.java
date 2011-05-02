package org.bbop.termgenie.core;

/**
 * Container for rules. Each rule has a function name.
 */
public class TemplateRule {

	private final String name;
	private final String value;

	/**
	 * @param name
	 * @param value
	 */
	public TemplateRule(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the rule String
	 */
	public String getValue() {
		return value;
	}
}
