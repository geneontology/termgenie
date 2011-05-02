package org.bbop.termgenie.core;

import java.util.Collections;
import java.util.List;

/**
 * Specifications for a field in an ontology term generation template.
 */
public class TemplateField implements OntologyAware {
	
	private final String name;
	private final boolean required;
	private final Cardinality cardinality;
	private final List<String> functionalPrefixes;
	private final Ontology correspondingOntology;

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
	 * Constant: Fields, which require at least two inputs of of the same type. 
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
	 * Specify the cardinality of a field.
	 */
	public static abstract class Cardinality
	{
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
	
	/**
	 * Convenience constructor, for fields without prefixes, single cardinality, 
	 * and no corresponding ontology.
	 * 
	 * @param name
	 * @param required
	 * @param correspondingOntology
	 */
	public TemplateField(String name, boolean required) {
		this(name, required, SINGLE_FIELD_CARDINALITY, null);
	}
	
	/**
	 * Convenience constructor, for fields without prefixes and single cardinality.
	 * 
	 * @param name
	 * @param required
	 * @param correspondingOntology
	 */
	public TemplateField(String name, boolean required, Ontology correspondingOntology) {
		this(name, required, SINGLE_FIELD_CARDINALITY, correspondingOntology);
	}
	
	
	/**
	 * Convenience constructor, for fields without prefixes.
	 * 
	 * @param name
	 * @param required
	 * @param cardinality
	 * @param correspondingOntology
	 */
	public TemplateField(String name, boolean required, Cardinality cardinality, Ontology correspondingOntology) {
		this(name, required, cardinality, null, correspondingOntology);
	}
	
	/**
	 * Standard constructor for specifying all parameters of a field.
	 * 
	 * @param name
	 * @param required
	 * @param cardinality
	 * @param functionalPrefixes
	 * @param correspondingOntology 
	 */
	public TemplateField(String name, boolean required, Cardinality cardinality, List<String> functionalPrefixes, Ontology correspondingOntology) {
		super();
		this.name = name;
		this.required = required;
		this.cardinality = cardinality;
		if (functionalPrefixes == null || functionalPrefixes.isEmpty()) {
			this.functionalPrefixes = Collections.emptyList();
		}
		else {
			this.functionalPrefixes = Collections.unmodifiableList(functionalPrefixes);
		}
		this.correspondingOntology = correspondingOntology;
	}

	/**
	 * @return name of this field.
	 */
	public String getName() {
		return name;
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
	 * @return list of functional prefix, to choose from. Never null, but returns empty lists. 
	 */
	public List<String> getFunctionalPrefixes() {
		return functionalPrefixes;
	}


	@Override
	public Ontology getCorrespondingOntology() {
		return correspondingOntology;
	}
}