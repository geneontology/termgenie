package org.bbop.termgenie.data;

import java.util.Arrays;

public class JsonTermGenerationParameter {

	private JsonOntologyTermIdentifier[][] terms;
	private String[][] strings;

	public JsonTermGenerationParameter() {
		super();
	}

	/**
	 * @return the terms
	 */
	public JsonOntologyTermIdentifier[][] getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	public void setTerms(JsonOntologyTermIdentifier[][] terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public String[][] getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	public void setStrings(String[][] strings) {
		this.strings = strings;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonTermGenerationParameter {");
		toString(terms, "terms", builder);
		toString(strings, "strings", builder);
		builder.append("}");
		return builder.toString();
	}

	private static void toString(Object[][] matrix, String name, StringBuilder builder) {
		if (matrix != null) {
			builder.append(name);
			builder.append(":{");
			for (int i = 0; i < matrix.length; i++) {
				if (i > 0) {
					builder.append(", ");
				}
				Object[] termList = matrix[i];
				if (termList == null) {
					builder.append("null");
				}
				else {
					builder.append(Arrays.toString(termList));
				}
			}
			builder.append("}, ");
		}
	}

	public static final class JsonOntologyTermIdentifier {

		private String ontology;
		private String termId;

		public JsonOntologyTermIdentifier() {
			super();
		}

		/**
		 * @param ontology
		 * @param termId
		 */
		public JsonOntologyTermIdentifier(String ontology, String termId) {
			super();
			this.ontology = ontology;
			this.termId = termId;
		}

		/**
		 * @return the ontology
		 */
		public String getOntology() {
			return ontology;
		}

		/**
		 * @param ontology the ontology to set
		 */
		public void setOntology(String ontology) {
			this.ontology = ontology;
		}

		/**
		 * @return the termId
		 */
		public String getTermId() {
			return termId;
		}

		/**
		 * @param termId the termId to set
		 */
		public void setTermId(String termId) {
			this.termId = termId;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonOntologyTermIdentifier:{");
			if (ontology != null) {
				builder.append("ontology:");
				builder.append(ontology);
				builder.append(", ");
			}
			if (termId != null) {
				builder.append("termId:");
				builder.append(termId);
			}
			builder.append("}");
			return builder.toString();
		}
	}
}
