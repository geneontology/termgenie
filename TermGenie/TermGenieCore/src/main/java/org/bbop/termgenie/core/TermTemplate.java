package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Data container for templates.
 */
public class TermTemplate {

	private final Ontology correspondingOntology;
	private final String name;
	private final String displayName;
	private final String description;
	private final List<TemplateField> fields;
	private final List<Ontology> external;
	private final List<String> requires;
	private final String obo_namespace;
	private final String rules;
	private final String hint;

	/**
	 * @param correspondingOntology
	 * @param name
	 * @param displayName
	 * @param description
	 * @param fields
	 * @param external
	 * @param requires
	 * @param obo_namespace
	 * @param rules
	 * @param hint
	 */
	public TermTemplate(Ontology correspondingOntology,
			String name,
			String displayName,
			String description,
			List<TemplateField> fields,
			List<Ontology> external,
			List<String> requires,
			String obo_namespace,
			String rules,
			String hint)
	{
		super();
		this.correspondingOntology = correspondingOntology;
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name, must never be empty");
		}
		this.name = name;
		this.displayName = displayName;
		if (fields == null || fields.isEmpty()) {
			throw new IllegalArgumentException("The field list, must never be empty");
		}
		this.fields = Collections.unmodifiableList(fields);
		this.external = external != null ? Collections.unmodifiableList(external) : null;
		this.requires = requires != null ? Collections.unmodifiableList(requires) : null;
		this.obo_namespace = obo_namespace;
		this.rules = rules;
		this.description = description;
		this.hint = hint;
	}

	public Ontology getCorrespondingOntology() {
		return correspondingOntology;
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
	 * @return the external
	 */
	public List<Ontology> getExternal() {
		return external;
	}

	/**
	 * @return the requires
	 */
	public List<String> getRequires() {
		return requires;
	}

	/**
	 * @return the obo_namespace
	 */
	public String getOboNamespace() {
		return obo_namespace;
	}

	/**
	 * @return the rules
	 */
	public String getRules() {
		return rules;
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
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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
		if (correspondingOntology != null) {
			builder.append("correspondingOntology=");
			builder.append(correspondingOntology);
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
