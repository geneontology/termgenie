package org.bbop.termgenie.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.data.JsonTermTemplate.JsonTemplateField;

public class JsonTermGenerationParameter {

	private JsonMultiValueMap<JsonOntologyTerm> terms;
	private JsonMultiValueMap<String> strings;
	private JsonMultiValueMap<List<String>> prefixes;

	public JsonTermGenerationParameter() {
		terms = new JsonMultiValueMap<JsonOntologyTerm>();
		strings = new JsonMultiValueMap<String>();
		prefixes = new JsonMultiValueMap<List<String>>();
	}

	/**
	 * @return the terms
	 */
	public JsonMultiValueMap<JsonOntologyTerm> getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	void setTerms(JsonMultiValueMap<JsonOntologyTerm> terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public JsonMultiValueMap<String> getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	void setStrings(JsonMultiValueMap<String> strings) {
		this.strings = strings;
	}
	
	/**
	 * @return the prefixes
	 */
	public JsonMultiValueMap<List<String>> getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	void setPrefixes(JsonMultiValueMap<List<String>> prefixes) {
		this.prefixes = prefixes;
	}



	public static final class JsonOntologyTerm {
		private String ontology;
		private String termId;

		public JsonOntologyTerm() {
			super();
		}

		/**
		 * @param ontology
		 * @param termId
		 */
		public JsonOntologyTerm(String ontology, String termId) {
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
	}
	
	public static class JsonMultiValueMap<V> {
		
		private Map<String, List<V>> values = new HashMap<String, List<V>>();
		
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
	}
}
