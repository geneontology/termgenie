package org.bbop.termgenie.core.rules;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
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

		private final OntologyTerm[][] terms;
		private final String[][] strings;
		
		/**
		 * @param terms
		 * @param strings
		 */
		public TermGenerationParameters(OntologyTerm[][] terms, String[][] strings) {
			super();
			this.terms = terms;
			this.strings = strings;
		}
		
		/**
		 * @param terms
		 * @param strings
		 */
		public TermGenerationParameters(int fieldCount) {
			this(new OntologyTerm[fieldCount][], new String[fieldCount][]);
		}

		/**
		 * @return the terms
		 */
		public OntologyTerm[][] getTerms() {
			return terms;
		}

		/**
		 * @return the strings
		 */
		public String[][] getStrings() {
			return strings;
		}
		
		public void setStringValues(TermTemplate template, String field, String...values) {
			setValues(strings, template, field, values);
		}
		
		public void setStringValues(TermTemplate template, int pos, String...values) {
			setValues(strings, template, pos, values);
		}
		
		public void setTermValues(TermTemplate template, String field, OntologyTerm...values) {
			setValues(terms, template, field, values);
		}
		
		public void setTermValues(TermTemplate template, int pos, OntologyTerm...values) {
			setValues(terms, template, pos, values);
		}

		private <T> void setValues(T[][] storeTo, TermTemplate template, String field, T...values) {
			int pos = template.getFieldPos(field);
			storeTo[pos] = values;
		}
		
		private <T> void setValues(T[][] storeTo, TermTemplate template, int pos, T...values) {
			storeTo[pos] = values;
		}
	}
}
