package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Data container for templates.
 */
public class TermTemplate implements OntologyAware {

	private final List<Ontology> correspondingOntologies;
	private final String name;
	private final String displayName;
	private final String description;
	private final List<TemplateField> fields;
	private final List<TemplateRule> rules;
	private final String hint;
	
	//TODO
	// what is the actual template?, How to handle automatic name generation?
	// who is allowed to use this rule?
	
	/**
	 * @param correspondingOntology
	 * @param name
	 * @param fields
	 * @param rules
	 */
	public TermTemplate(Ontology correspondingOntology, String name, String displayName, String description,
			List<TemplateField> fields, List<TemplateRule> rules, String hint) {
		super();
		this.correspondingOntologies = Collections.singletonList(correspondingOntology);
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name, must never be empty");
		}
		this.name = name;
		this.displayName = displayName;
		if (fields == null || fields.isEmpty()) {
			throw new IllegalArgumentException("The field list, must never be empty");
		}
		this.fields = Collections.unmodifiableList(fields);
		this.rules = rules;
		this.description = description;
		this.hint = hint;
	}

	@Override
	public List<Ontology> getCorrespondingOntologies() {
		return correspondingOntologies;
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
	 * @return the rules
	 */
	public List<TemplateRule> getRules() {
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
	
	/**
	 * Retrieve the field pos for a given name.
	 * 
	 * @param name
	 * @return pos or -1 if not existent
	 */
	public int getFieldPos(String name) {
		if (name == null) {
			return -1;
		}
		for (int i = 0; i < fields.size(); i++) {
			TemplateField field = fields.get(i);
			if (field.getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Retrieve field count for the template.
	 * 
	 * @return count
	 */
	public int getFieldCount() {
		return fields.size();
	}

	/* (non-Javadoc)
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
		if (correspondingOntologies != null) {
			builder.append("correspondingOntologies=");
			builder.append(correspondingOntologies);
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
		if (rules != null) {
			builder.append("rules=");
			builder.append(rules);
			builder.append(", ");
		}
		if (hint != null) {
			builder.append("hint=");
			builder.append(hint);
		}
		builder.append("]");
		return builder.toString();
	}
}
