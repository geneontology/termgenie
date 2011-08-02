package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TermGenerationInput [");
			if (termTemplate != null) {
				builder.append("termTemplate=");
				builder.append(termTemplate);
				builder.append(", ");
			}
			if (parameters != null) {
				builder.append("parameters=");
				builder.append(parameters);
			}
			builder.append("]");
			return builder.toString();
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TermGenerationOutput [");
			builder.append("success=");
			builder.append(success);
			if (term != null) {
				builder.append(", ");
				builder.append("term=");
				builder.append(term);
			}
			if (message != null) {
				builder.append(", ");
				builder.append("message=");
				builder.append(message);
			}
			if (input != null) {
				builder.append(", ");
				builder.append("input=");
				builder.append(input);
			}
			builder.append("]");
			return builder.toString();
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
		 * @param fieldCount
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
		
		/**
		 * @param template
		 * @param field
		 * @param values
		 */
		public void setStringValues(TermTemplate template, String field, String...values) {
			setValues(strings, template, field, values);
		}
		
		/**
		 * @param template
		 * @param pos
		 * @param values
		 */
		public void setStringValues(TermTemplate template, int pos, String...values) {
			setValues(strings, template, pos, values);
		}
		
		/**
		 * @param template
		 * @param field
		 * @param values
		 */
		public void setTermValues(TermTemplate template, String field, OntologyTerm...values) {
			setValues(terms, template, field, values);
		}
		
		/**
		 * @param template
		 * @param pos
		 * @param values
		 */
		public void setTermValues(TermTemplate template, int pos, OntologyTerm...values) {
			setValues(terms, template, pos, values);
		}

		/**
		 * @param <T>
		 * @param storeTo
		 * @param template
		 * @param field
		 * @param values
		 */
		private <T> void setValues(T[][] storeTo, TermTemplate template, String field, T...values) {
			int pos = template.getFieldPos(field);
			storeTo[pos] = values;
		}
		
		/**
		 * @param <T>
		 * @param storeTo
		 * @param template
		 * @param pos
		 * @param values
		 */
		private <T> void setValues(T[][] storeTo, TermTemplate template, int pos, T...values) {
			storeTo[pos] = values;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TermGenerationParameters [");
			if (terms != null) {
				builder.append("terms=");
				builder.append(Arrays.toString(terms));
				builder.append(", ");
			}
			if (strings != null) {
				builder.append("strings=");
				builder.append(Arrays.toString(strings));
			}
			builder.append("]");
			return builder.toString();
		}
	}
}
