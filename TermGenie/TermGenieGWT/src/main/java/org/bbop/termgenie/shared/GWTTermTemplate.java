package org.bbop.termgenie.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class is required to map the internal representation of fields to an object which
 * can be serialized by GWT.
 */
public class GWTTermTemplate implements IsSerializable {

	private String name;
	private GWTTemplateField[] fields;

	public GWTTermTemplate() {
		super();
	}

	/**
	 * @param name
	 * @param fields
	 */
	public GWTTermTemplate(String name, GWTTemplateField[] fields) {
		super();
		this.name = name;
		this.fields = fields;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the fields
	 */
	public GWTTemplateField[] getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(GWTTemplateField[] fields) {
		this.fields = fields;
	}

	public static class GWTTemplateField implements IsSerializable {
		private String name;
		private boolean required;
		private GWTCardinality cardinality;
		private String[] functionalPrefixes;
		private String ontology;

		public GWTTemplateField() {
			super();
		}

		/**
		 * @param name
		 * @param required
		 * @param cardinality
		 * @param functionalPrefixes
		 * @param ontology
		 */
		public GWTTemplateField(String name, boolean required, GWTCardinality cardinality,
				String[] functionalPrefixes, String ontology) {
			super();
			this.name = name;
			this.required = required;
			this.cardinality = cardinality;
			this.functionalPrefixes = functionalPrefixes;
			this.ontology = ontology;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return required;
		}

		/**
		 * @param required
		 *            the required to set
		 */
		public void setRequired(boolean required) {
			this.required = required;
		}

		/**
		 * @return the cardinality
		 */
		public GWTCardinality getCardinality() {
			return cardinality;
		}

		/**
		 * @param cardinality
		 *            the cardinality to set
		 */
		public void setCardinality(GWTCardinality cardinality) {
			this.cardinality = cardinality;
		}

		/**
		 * @return the functionalPrefixes
		 */
		public String[] getFunctionalPrefixes() {
			return functionalPrefixes;
		}

		/**
		 * @param functionalPrefixes
		 *            the functionalPrefixes to set
		 */
		public void setFunctionalPrefixes(String[] functionalPrefixes) {
			this.functionalPrefixes = functionalPrefixes;
		}

		/**
		 * @return the ontology
		 */
		public String getOntology() {
			return ontology;
		}

		/**
		 * @param ontology
		 *            the ontology to set
		 */
		public void setOntology(String ontology) {
			this.ontology = ontology;
		}
	}

	public static class GWTCardinality implements IsSerializable {
		private int min;
		private int max;

		public GWTCardinality() {
			super();
		}

		/**
		 * @param min
		 * @param max
		 */
		public GWTCardinality(int min, int max) {
			super();
			this.min = min;
			this.max = max;
		}

		/**
		 * @return the min
		 */
		public int getMin() {
			return min;
		}

		/**
		 * @param min
		 *            the min to set
		 */
		public void setMin(int min) {
			this.min = min;
		}

		/**
		 * @return the max
		 */
		public int getMax() {
			return max;
		}

		/**
		 * @param max
		 *            the max to set
		 */
		public void setMax(int max) {
			this.max = max;
		}
	}
}
