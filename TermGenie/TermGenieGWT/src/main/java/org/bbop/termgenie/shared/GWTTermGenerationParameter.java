package org.bbop.termgenie.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTTermGenerationParameter implements IsSerializable {

	private MultiValueMap<OntologyTerm> terms;
	private MultiValueMap<String> strings;
	private MultiValueMap<List<String>> prefixes;

	public GWTTermGenerationParameter() {
		terms = new MultiValueMap<OntologyTerm>();
		strings = new MultiValueMap<String>();
		prefixes = new MultiValueMap<List<String>>();
	}

	/**
	 * @return the terms
	 */
	public MultiValueMap<OntologyTerm> getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	void setTerms(MultiValueMap<OntologyTerm> terms) {
		this.terms = terms;
	}

	/**
	 * @return the strings
	 */
	public MultiValueMap<String> getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	void setStrings(MultiValueMap<String> strings) {
		this.strings = strings;
	}
	
	/**
	 * @return the prefixes
	 */
	public MultiValueMap<List<String>> getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	void setPrefixes(MultiValueMap<List<String>> prefixes) {
		this.prefixes = prefixes;
	}



	public static final class OntologyTerm implements IsSerializable {
		private String ontology;
		private String termId;

		public OntologyTerm() {
			super();
		}

		/**
		 * @param ontology
		 * @param termId
		 */
		public OntologyTerm(String ontology, String termId) {
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
	
	public static class MultiValueMap<V> implements IsSerializable{
		
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
