package org.bbop.termgenie.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.data.JsonTermTemplate.JsonTemplateField;

public class JsonTermGenerationParameter {

	private JsonMultiValueMapJsonOntologyTerm terms;
	private JsonMultiValueMapString strings;
	private JsonMultiValueMapListString prefixes;

	public JsonTermGenerationParameter() {
		terms = new JsonMultiValueMapJsonOntologyTerm();
		strings = new JsonMultiValueMapString();
		prefixes = new JsonMultiValueMapListString();
	}

	/**
	 * @return the terms
	 */
	public JsonMultiValueMapJsonOntologyTerm getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	void setTerms(JsonMultiValueMapJsonOntologyTerm terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public JsonMultiValueMapString getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	void setStrings(JsonMultiValueMapString strings) {
		this.strings = strings;
	}
	
	/**
	 * @return the prefixes
	 */
	public JsonMultiValueMapListString getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	void setPrefixes(JsonMultiValueMapListString prefixes) {
		this.prefixes = prefixes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonTermGenerationParameter:{");
		if (terms != null) {
			builder.append("terms: ");
			builder.append(terms);
			builder.append(", ");
		}
		if (strings != null) {
			builder.append("strings: ");
			builder.append(strings);
			builder.append(", ");
		}
		if (prefixes != null) {
			builder.append("prefixes: ");
			builder.append(prefixes);
		}
		builder.append("}");
		return builder.toString();
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
	
	public static class JsonMultiValueMapJsonOntologyTerm extends JsonMultiValueMap<JsonOntologyTermIdentifier> {
	}
	
	public static class JsonMultiValueMapString extends JsonMultiValueMap<String> {
	}
	
	public static class JsonMultiValueMapListString extends JsonMultiValueMap<List<String>> {
	}
	
	public abstract static class JsonMultiValueMap<V> {
		
		protected Map<String, List<V>> values = new HashMap<String, List<V>>();
		
		public V getValue(JsonTemplateField key, int pos) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null && list.size() > pos) {
				return list.get(pos);
			}
			return null;
		}

		public int getCount(JsonTemplateField key) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null) {
				return list.size();
			}
			return 0;
		}
		
		public void addValue(V value, JsonTemplateField key, int pos) {
			final String internalKey = calculateInteralKey(key);
			List<V> list = values.get(internalKey);
			if (list == null) {
				list = new ArrayList<V>(pos + 1);
				for (int i = 0; i < pos; i++) {
					list.add(null);
				}
				list.add(value);
				values.put(internalKey, list);
			} else {
				for (int i = (list.size()) - 1; i < pos; i++) {
					list.add(null);
				}
				list.add(value);
			}
		}
		
		public String calculateInteralKey(JsonTemplateField key) {
			return key.getName();
		}
		
		/**
		 * @return the values
		 */
		Map<String, List<V>> getValues() {
			return values;
		}

		/**
		 * @param values the values to set
		 */
		void setValues(Map<String, List<V>> values) {
			this.values = values;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			final int maxLen = 10;
			StringBuilder builder = new StringBuilder();
			builder.append("JsonMultiValueMap {");
			if (values != null) {
				builder.append("values=");
				builder.append(toString(values.entrySet(), maxLen));
			}
			builder.append("}");
			return builder.toString();
		}

		private String toString(Collection<?> collection, int maxLen) {
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			int i = 0;
			for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
				if (i > 0)
					builder.append(", ");
				builder.append(iterator.next());
			}
			builder.append("}");
			return builder.toString();
		}
	}
}
