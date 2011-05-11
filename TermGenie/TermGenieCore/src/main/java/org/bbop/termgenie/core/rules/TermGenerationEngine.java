package org.bbop.termgenie.core.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;

public interface TermGenerationEngine {

	public List<OntologyTerm> generateTerms(TermTemplate templateName, TermGenerationParameters parameters);
	
	public final class TermGenerationParameters {
		
		private final MultiValueMap<OntologyTerm> terms;
		private final MultiValueMap<String> strings;
		private final MultiValueMap<List<String>> prefixes;

		public TermGenerationParameters() {
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
		 * @return the strings
		 */
		public MultiValueMap<String> getStrings() {
			return strings;
		}

		/**
		 * @return the prefixes
		 */
		public MultiValueMap<List<String>> getPrefixes() {
			return prefixes;
		}
	}
	
	public static class MultiValueMap<V>{
		
		private Map<String, List<V>> values = new HashMap<String, List<V>>();
		
		public V getValue(TemplateField key, int pos) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null && list.size() > pos) {
				return list.get(pos);
			}
			return null;
		}

		public int getCount(TemplateField key) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null) {
				return list.size();
			}
			return 0;
		}
		
		public void addValue(V value, TemplateField key, int pos) {
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
		
		public String calculateInteralKey(TemplateField key) {
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
