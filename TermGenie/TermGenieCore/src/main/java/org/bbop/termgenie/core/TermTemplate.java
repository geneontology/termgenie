package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Data container for templates.
 */
public class TermTemplate implements OntologyAware {

	private final Ontology correspondingOntology;
	private final String name;
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
	public TermTemplate(Ontology correspondingOntology, String name,
			List<TemplateField> fields, List<TemplateRule> rules) {
		super();
		this.correspondingOntology = correspondingOntology;
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("The name, must never be empty");
		}
		this.name = name;
		if (fields == null || fields.isEmpty()) {
			throw new IllegalArgumentException("The field list, must never be empty");
		}
		this.fields = Collections.unmodifiableList(fields);
		this.rules = rules;
	}

	@Override
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
	 * @return the rules
	 */
	public List<TemplateRule> getRules() {
		return rules;
	}
}
