package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Specifications for a field in an ontology term generation template.
 */
public class TemplateField {

	private final String name;
	private final String label;
	private final boolean required;
	private final Cardinality cardinality;
	private final List<String> functionalPrefixes;
	private final List<Ontology> correspondingOntologies;

	/**
	 * Constant: Fields, which require exactly one input.
	 */
	public static final Cardinality SINGLE_FIELD_CARDINALITY = new Cardinality() {

		@Override
		public int getMinimum() {
			return 1;
		}

		@Override
		public int getMaximum() {
			return 1;
		}
	};

	/**
	 * Constant: Fields, which require at least two inputs of the same type.
	 */
	public static final Cardinality TWO_TO_N_CARDINALITY = new Cardinality() {

		@Override
		public int getMinimum() {
			return 2;
		}

		@Override
		public int getMaximum() {
			return Integer.MAX_VALUE;
		}
	};

	/**
	 * Constant: Fields, which require at least one input.
	 */
	public static final Cardinality ONE_TO_N_CARDINALITY = new Cardinality() {

		@Override
		public int getMinimum() {
			return 1;
		}

		@Override
		public int getMaximum() {
			return Integer.MAX_VALUE;
		}
	};

	/**
	 * Specify the cardinality of a field.
	 */
	public static abstract class Cardinality {

		public abstract int getMinimum();

		public abstract int getMaximum();

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getMinimum());
			sb.append(getMaximum() == Integer.MAX_VALUE ? "N" : Integer.toString(getMaximum()));
			return sb.toString();
		}
	}

	public TemplateField(String name, Ontology ontology) {
		this(name, null, true, ontology == null ? null : Collections.singletonList(ontology));
	}

	public TemplateField(String name, List<Ontology> ontologies) {
		this(name, null, true, ontologies);
	}

	/**
	 * Convenience constructor, for fields without prefixes, single cardinality,
	 * no corresponding ontology, and no label.
	 * 
	 * @param name
	 */
	public TemplateField(String name) {
		this(name, null, false, SINGLE_FIELD_CARDINALITY, null);
	}
	
	/**
	 * Convenience constructor, for fields without prefixes, single cardinality,
	 * and no corresponding ontology.
	 * 
	 * @param name
	 * @param label
	 */
	public TemplateField(String name, String label) {
		this(name, label, false, SINGLE_FIELD_CARDINALITY, null);
	}

	/**
	 * Convenience constructor, for fields without prefixes and single
	 * cardinality.
	 * 
	 * @param name
	 * @param label
	 * @param required
	 * @param correspondingOntologies
	 */
	public TemplateField(String name, String label, boolean required, List<Ontology> correspondingOntologies) {
		this(name, label, required, SINGLE_FIELD_CARDINALITY, correspondingOntologies);
	}

	/**
	 * Convenience constructor, for fields without prefixes.
	 * 
	 * @param name
	 * @param label
	 * @param required
	 * @param cardinality
	 * @param correspondingOntologies
	 */
	public TemplateField(String name,
			String label,
			boolean required,
			Cardinality cardinality,
			List<Ontology> correspondingOntologies)
	{
		this(name, label, required, cardinality, null, correspondingOntologies);
	}

	/**
	 * Standard constructor for specifying all parameters of a field.
	 * 
	 * @param name
	 * @param label
	 * @param required
	 * @param cardinality
	 * @param functionalPrefixes
	 * @param correspondingOntology
	 */
	public TemplateField(String name,
			String label,
			boolean required,
			Cardinality cardinality,
			List<String> functionalPrefixes,
			Ontology correspondingOntology)
	{
		this(name, label, required, cardinality, functionalPrefixes, correspondingOntology == null ? null : Collections.singletonList(correspondingOntology));
	}

	/**
	 * Standard constructor for specifying all parameters of a field.
	 * 
	 * @param name
	 * @param label
	 * @param required
	 * @param cardinality
	 * @param functionalPrefixes
	 * @param correspondingOntologies
	 */
	public TemplateField(String name,
			String label,
			boolean required,
			Cardinality cardinality,
			List<String> functionalPrefixes,
			List<Ontology> correspondingOntologies)
	{
		super();
		this.name = name;
		this.label = label;
		this.required = required;
		this.cardinality = cardinality;
		if (functionalPrefixes == null || functionalPrefixes.isEmpty()) {
			this.functionalPrefixes = Collections.emptyList();
		}
		else {
			this.functionalPrefixes = Collections.unmodifiableList(functionalPrefixes);
		}
		this.correspondingOntologies = correspondingOntologies;
	}

	/**
	 * @return name of this field.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return true if this field is a required parameter, otherwise false
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @return cardinality
	 */
	public Cardinality getCardinality() {
		return cardinality;
	}

	/**
	 * @return list of functional prefix, to choose from. Never null, but
	 *         returns empty lists.
	 */
	public List<String> getFunctionalPrefixes() {
		return functionalPrefixes;
	}

	public List<Ontology> getCorrespondingOntologies() {
		return correspondingOntologies;
	}

	public boolean hasCorrespondingOntologies() {
		return correspondingOntologies != null && !correspondingOntologies.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateField [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		builder.append("required=");
		builder.append(required);
		builder.append(", ");
		if (cardinality != null) {
			builder.append("cardinality=");
			builder.append(cardinality);
			builder.append(", ");
		}
		if (functionalPrefixes != null) {
			builder.append("functionalPrefixes=");
			builder.append(functionalPrefixes);
			builder.append(", ");
		}
		if (correspondingOntologies != null) {
			builder.append("correspondingOntologies=");
			builder.append(correspondingOntologies);
		}
		builder.append("]");
		return builder.toString();
	}
}
