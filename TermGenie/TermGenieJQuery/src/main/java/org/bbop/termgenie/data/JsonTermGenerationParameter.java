package org.bbop.termgenie.data;

public class JsonTermGenerationParameter {

	private JsonOntologyTermIdentifier[][] termLists;
	private String[][] stringLists;

	public JsonTermGenerationParameter() {
		super();
	}

	/**
	 * @return the termLists
	 */
	public JsonOntologyTermIdentifier[][] getTermLists() {
		return termLists;
	}

	/**
	 * @param termLists the termLists to set
	 */
	public void setTermLists(JsonOntologyTermIdentifier[][] termLists) {
		this.termLists = termLists;
	}

	/**
	 * @return the stringLists
	 */
	public String[][] getStringLists() {
		return stringLists;
	}

	/**
	 * @param stringLists the stringLists to set
	 */
	public void setStringLists(String[][] stringLists) {
		this.stringLists = stringLists;
	}

//	public static <V> V getValue(HashMap<String, V[]> values, JsonTemplateField key, int pos) {
//		V[] list = values.get(key.getName());
//		if (list != null && list.length > pos) {
//			return list[pos];
//		}
//		return null;
//	}
//	
//	public static <V> int getCount(HashMap<String, V[]> values, JsonTemplateField key) {
//		V[] list = values.get(key.getName());
//		if (list != null) {
//			return list.length;
//		}
//		return 0;
//	}
	

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
		 * @param ontology
		 *            the ontology to set
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
		 * @param termId
		 *            the termId to set
		 */
		public void setTermId(String termId) {
			this.termId = termId;
		}

		/* (non-Javadoc)
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
