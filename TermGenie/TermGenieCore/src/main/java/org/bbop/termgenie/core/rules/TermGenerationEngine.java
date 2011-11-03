package org.bbop.termgenie.core.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.TermTemplate;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

public interface TermGenerationEngine {

	/**
	 * Generate term candidates for the given ontology, the corresponding
	 * templates and the parameters.
	 * 
	 * @param ontology
	 * @param generationTasks
	 * @return candidates
	 */
	public List<TermGenerationOutput> generateTerms(Ontology ontology,
			List<TermGenerationInput> generationTasks);

	/**
	 * Retrieve all patterns available for this term generation engine.
	 * 
	 * @return templates
	 */
	public List<TermTemplate> getAvailableTemplates();

	/**
	 * Retrieve the prefix for temporary identifiers. Used during the commit to
	 * replace them with new valid identifiers.
	 * 
	 * @param ontology 
	 * 
	 * @return prefix
	 */
	public String getTempIdPrefix(OWLGraphWrapper ontology);

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

		private final OntologyTerm<ISynonym, IRelation> term;
		private final List<IRelation> changedTermRelations;
		private final TermGenerationInput input;
		private final boolean success;
		private final String message;

		public static TermGenerationOutput error(TermGenerationInput input, String message) {
			return new TermGenerationOutput(null, null, input, false, message);
		}
		
		/**
		 * @param term
		 * @param changedTermRelations 
		 * @param input
		 * @param success
		 * @param message
		 */
		public TermGenerationOutput(OntologyTerm<ISynonym, IRelation> term,
				List<IRelation> changedTermRelations,
				TermGenerationInput input,
				boolean success,
				String message)
		{
			super();
			this.term = term;
			this.changedTermRelations = changedTermRelations;
			this.input = input;
			this.success = success;
			this.message = message;
		}

		/**
		 * @return the term
		 */
		public OntologyTerm<ISynonym, IRelation> getTerm() {
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
		
		/**
		 * @return the changedTermRelations
		 */
		public final List<IRelation> getChangedTermRelations() {
			return changedTermRelations;
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
			if (changedTermRelations != null) {
				builder.append(", ");
				builder.append("changedTermRelations=");
				builder.append(changedTermRelations);
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

		private final Map<String, List<OntologyTerm<ISynonym, IRelation>>> terms;
		private final Map<String, List<String>> strings;

		public TermGenerationParameters() {
			super();
			this.terms = new HashMap<String, List<OntologyTerm<ISynonym,IRelation>>>();
			this.strings = new HashMap<String, List<String>>();
		}

		/**
		 * @return the terms
		 */
		public Map<String, List<OntologyTerm<ISynonym, IRelation>>> getTerms() {
			return terms;
		}

		/**
		 * @return the strings
		 */
		public Map<String, List<String>> getStrings() {
			return strings;
		}

		/**
		 * @param field
		 * @param values
		 */
		public void setStringValues(String field, List<String> values) {
			strings.put(field, values);
		}

		/**
		 * @param field
		 * @param values
		 */
		public void setTermValues(String field, List<OntologyTerm<ISynonym, IRelation>> values) {
			terms.put(field, values);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TermGenerationParameters [");
			if (terms != null) {
				builder.append("terms=");
				builder.append(terms);
				builder.append(", ");
			}
			if (strings != null) {
				builder.append("strings=");
				builder.append(strings);
			}
			builder.append("]");
			return builder.toString();
		}
	}
}
