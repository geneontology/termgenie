package org.bbop.termgenie.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTTermGenerationParameter implements IsSerializable {

	private GWTMultiValueMap<GWTOntologyTerm> terms;
	private GWTMultiValueMap<String> strings;

	public GWTTermGenerationParameter() {
		terms = new GWTMultiValueMap<GWTOntologyTerm>();
		strings = new GWTMultiValueMap<String>();
	}

	/**
	 * @return the terms
	 */
	public GWTMultiValueMap<GWTOntologyTerm> getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	void setTerms(GWTMultiValueMap<GWTOntologyTerm> terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public GWTMultiValueMap<String> getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	void setStrings(GWTMultiValueMap<String> strings) {
		this.strings = strings;
	}
	
	public static final class GWTOntologyTerm implements IsSerializable {
		private String ontology;
		private String termId;

		public GWTOntologyTerm() {
			super();
		}

		/**
		 * @param ontology
		 * @param termId
		 */
		public GWTOntologyTerm(String ontology, String termId) {
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
	
	public static class GWTMultiValueMap<V> implements IsSerializable{
		
		private Map<String, List<V>> values = new HashMap<String, List<V>>();
		
		public V getValue(GWTTemplateField key, int pos) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null && list.size() > pos) {
				return list.get(pos);
			}
			return null;
		}

		public int getCount(GWTTemplateField key) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null) {
				return list.size();
			}
			return 0;
		}
		
		public void addValue(V value, GWTTemplateField key, int pos) {
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
		
		public void setValues(List<V> values, GWTTemplateField key) {
			final String internalKey = calculateInteralKey(key);
			this.values.put(internalKey, new ArrayList<V>(values));
		}
		
		public String calculateInteralKey(GWTTemplateField key) {
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
