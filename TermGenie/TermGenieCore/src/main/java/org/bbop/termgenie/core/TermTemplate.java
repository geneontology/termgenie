package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Data container for templates.
 */
public class TermTemplate {

	private final String name;
	private final String displayName;
	private final String description;
	private final List<String> categories;
	private final List<TemplateField> fields;
	private final String obo_namespace;
	private final List<String> ruleFiles;
	private final String methodName;
	private final String hint;

	/**
	 * @param name
	 * @param displayName
	 * @param description
	 * @param fields
	 * @param obo_namespace
	 * @param ruleFiles
	 * @param methodName 
	 * @param hint
	 * @param categories
	 */
	public TermTemplate(String name,
			String displayName,
			String description,
			List<TemplateField> fields,
			String obo_namespace,
			List<String> ruleFiles,
			String methodName,
			String hint,
			List<String> categories)
	{
		super();
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name, must never be empty");
		}
		this.name = name;
		this.displayName = displayName;
		if (fields == null || fields.isEmpty()) {
			throw new IllegalArgumentException("The field list, must never be empty");
		}
		this.fields = Collections.unmodifiableList(fields);
		this.obo_namespace = obo_namespace;
		this.ruleFiles = ruleFiles;
		this.methodName = methodName;
		this.description = description;
		this.hint = hint;
		this.categories = categories;
	}

	/**
	 * @return the name, never null or empty
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the fields, never null or empty, unmodifiable list
	 */
	public List<TemplateField> getFields() {
		return fields;
	}

	/**
	 * @return the obo_namespace
	 */
	public String getOboNamespace() {
		return obo_namespace;
	}

	/**
	 * @return the ruleFiles
	 */
	public List<String> getRuleFiles() {
		return ruleFiles;
	}
	
	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Retrieve the field for a given name.
	 * 
	 * @param name
	 * @return field or null
	 */
	public TemplateField getField(String name) {
		if (name == null) {
			return null;
		}
		for (TemplateField field : fields) {
			if (field.getName().equals(name) || name.equals(field.getLabel())) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * @return the categories
	 */
	public List<String> getCategories() {
		return categories;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TermTemplate [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("displayName=");
			builder.append(displayName);
			builder.append(", ");
		}
		if (description != null) {
			builder.append("description=");
			builder.append(description);
			builder.append(", ");
		}
		if (fields != null) {
			builder.append("fields=");
			builder.append(fields);
			builder.append(", ");
		}
		// do not render external, they polute the output
		/*
		 * if (external != null) { builder.append("external=");
		 * builder.append(external); builder.append(", "); }
		 */
		// do not render rules, they polute the output
		/*
		 * if (rules != null) { builder.append("rules="); builder.append(rules);
		 * builder.append(", "); }
		 */
		if (hint != null) {
			builder.append("hint=");
			builder.append(hint);
		}
		builder.append("]");
		return builder.toString();
	}
}
