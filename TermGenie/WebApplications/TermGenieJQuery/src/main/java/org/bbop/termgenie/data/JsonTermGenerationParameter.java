package org.bbop.termgenie.data;

import java.util.List;
import java.util.Map;

public class JsonTermGenerationParameter {

	private Map<String, List<JsonOntologyTermIdentifier>> terms;
	private Map<String, List<String>> strings;

	public JsonTermGenerationParameter() {
		super();
	}

	/**
	 * @return the terms
	 */
	public Map<String, List<JsonOntologyTermIdentifier>> getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	public void setTerms(Map<String, List<JsonOntologyTermIdentifier>> terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public Map<String, List<String>> getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	public void setStrings(Map<String, List<String>> strings) {
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

	private static <T> void toString(Map<String, List<T>> matrix, String name, StringBuilder builder) {
		if (matrix != null) {
			builder.append(name);
			builder.append(":{");
			boolean first = true;
			for(String field : matrix.keySet()) {
				if (first) {
					first = false;
				}
				else {
					builder.append(", ");
				}
				List<T> values = matrix.get(field);
				if (values == null) {
					builder.append("null");
				}
				else {
					builder.append(values);
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
