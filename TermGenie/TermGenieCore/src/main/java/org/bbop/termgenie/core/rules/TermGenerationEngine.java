package org.bbop.termgenie.core.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;

public interface TermGenerationEngine {

	/**
	 * Generate term candidates for the given ontology, the corresponding
	 * templates and the parameters.
	 * 
	 * @param ontology
	 * @param generationTasks
	 * @return candidates
	 */
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks);

	/**
	 * Retrieve all patterns available for this term generation engine. 
	 * 
	 * @return templates
	 */
	public List<TermTemplate> getAvailableTemplates();
	
	
	public final class TermGenerationInput {

		private final TermTemplate termTemplate;
		private final TermGenerationParameters parameters;

		/**
		 * @param termTemplate
		 * @param parameters
		 */
		public TermGenerationInput(TermTemplate termTemplate, TermGenerationParameters parameters) {
			super();
			this.termTemplate = termTemplate;
			this.parameters = parameters;
		}

		/**
		 * @return the termTemplate
		 */
		public TermTemplate getTermTemplate() {
			return termTemplate;
		}

		/**
		 * @return the parameters
		 */
		public TermGenerationParameters getParameters() {
			return parameters;
		}
	}

	public class TermGenerationOutput {

		private final OntologyTerm term;
		private final TermGenerationInput input;
		private final boolean success;
		private final String message;

		/**
		 * @param term
		 * @param input
		 * @param success
		 * @param message
		 */
		public TermGenerationOutput(OntologyTerm term, TermGenerationInput input, boolean success, String message) {
			super();
			this.term = term;
			this.input = input;
			this.success = success;
			this.message = message;
		}

		/**
		 * @return the term
		 */
		public OntologyTerm getTerm() {
			return term;
		}

		/**
		 * @return the input
		 */
		public TermGenerationInput getInput() {
			return input;
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}
	}

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

	public static class MultiValueMap<V> {

		private Map<String, ArrayList<V>> values = new HashMap<String, ArrayList<V>>();

		public V getValue(TemplateField key, int pos) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null && list.size() > pos) {
				return list.get(pos);
			}
			return null;
		}
		
		public V getValue(String field, int pos) {
			List<V> list = values.get(field);
			if (list != null && list.size() > pos) {
				return list.get(pos);
			}
			return null;
		}
		
		public List<V> getValues(String field) {
			return values.get(field);
		}
		
		public List<V> getValues(TemplateField key) {
			List<V> list = values.get(calculateInteralKey(key));
			return list;
		}

		public int getCount(TemplateField key) {
			List<V> list = values.get(calculateInteralKey(key));
			if (list != null) {
				return list.size();
			}
			return 0;
		}
		
		public int getCount(String field) {
			List<V> list = values.get(field);
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		public void addValue(V value, TemplateField key, int pos) {
			final String internalKey = calculateInteralKey(key);
			ArrayList<V> list = values.get(internalKey);
			if (list == null) {
				list = new ArrayList<V>(pos + 1);
				values.put(internalKey, list);
			}
			assertSize(list, pos + 1);
			list.set(pos, value);
		}
		
		private void assertSize(ArrayList<V> list, int size) {
			while (list.size() < size) {
				list.add(null);
			}
		}

		public String calculateInteralKey(TemplateField key) {
			return key.getName();
		}

		/**
		 * @return the values
		 */
		Map<String, ArrayList<V>> getValues() {
			return values;
		}

		/**
		 * @param values
		 *            the values to set
		 */
		void setValues(Map<String, ArrayList<V>> values) {
			this.values = values;
		}
	}
}
