package org.bbop.termgenie.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTTermGenerationParameter implements IsSerializable {

	private MultiValueMap<OntologyTerm> values;
	private MultiValueMap<String> strings;

	public GWTTermGenerationParameter() {
		values = new MultiValueMap<OntologyTerm>();
		strings = new MultiValueMap<String>();
	}

	public OntologyTerm getOntologyTerm(GWTTemplateField field, int pos) {
		return values.getValue(field, pos);
	}

	public int getCount(GWTTemplateField field) {
		return values.getCount(field);
	}

	public String getStringValue(GWTTemplateField field, int pos) {
		return strings.getValue(field, pos);
	}

	public void addOntologyTerm(OntologyTerm ontologyTerm, GWTTemplateField field, int pos) {
		values.addValue(ontologyTerm, field, pos);
	}

	public void addString(String value, GWTTemplateField field, int pos) {
		strings.addValue(value, field, pos);
	}

	/**
	 * @return the values
	 */
	MultiValueMap<OntologyTerm> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	void setValues(MultiValueMap<OntologyTerm> values) {
		this.values = values;
	}

	/**
	 * @return the strings
	 */
	MultiValueMap<String> getStrings() {
		return strings;
	}

	/**
	 * @param strings the strings to set
	 */
	void setStrings(MultiValueMap<String> strings) {
		this.strings = strings;
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
