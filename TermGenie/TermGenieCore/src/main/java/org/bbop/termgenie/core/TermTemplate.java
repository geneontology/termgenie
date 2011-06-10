package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Data container for templates.
 */
public class TermTemplate implements OntologyAware {

	private final List<Ontology> correspondingOntologies;
	private final String name;
	private final String description;
	private final List<TemplateField> fields;
	private final List<TemplateRule> rules;
	
	//TODO
	// what is the actual template?, How to handle automatic name generation?
	// who is allowed to use this rule?
	
	/**
	 * @param correspondingOntology
	 * @param name
	 * @param fields
	 * @param rules
	 */
	public TermTemplate(Ontology correspondingOntology, String name, String description,
			List<TemplateField> fields, List<TemplateRule> rules) {
		super();
		this.correspondingOntologies = Collections.singletonList(correspondingOntology);
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name, must never be empty");
		}
		this.name = name;
		if (fields == null || fields.isEmpty()) {
			throw new IllegalArgumentException("The field list, must never be empty");
		}
		this.fields = Collections.unmodifiableList(fields);
		this.rules = rules;
		this.description = description;
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
		}
		builder.append("]");
		return builder.toString();
	}
}
