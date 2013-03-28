package org.bbop.termgenie.core.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.OWLGraphWrapper;

public interface TermGenerationEngine {

	/**
	 * Generate term candidates for the given ontology, the corresponding
	 * templates and the parameters.
	 * 
	 * @param ontology
	 * @param generationTasks
	 * @param requireLiteratureReference
	 * @param processState
	 * @return candidates
	 */
	public List<TermGenerationOutput> generateTerms(Ontology ontology,
			List<TermGenerationInput> generationTasks,
			boolean requireLiteratureReference,
			ProcessState processState);

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

		private final Frame term;
		private final Set<OWLAxiom> owlAxioms;
		private final List<Pair<Frame, Set<OWLAxiom>>> changedTermRelations;
		private final TermGenerationInput input;
		
		private final List<String> warnings;
		private final String error;

		public static TermGenerationOutput error(TermGenerationInput input, String message) {
			return new TermGenerationOutput(null, null, null, input, message, null);
		}
		
		/**
		 * @param term
		 * @param owlAxioms
		 * @param changedTermRelations 
		 * @param input
		 * @param error
		 * @param warnings 
		 */
		public TermGenerationOutput(Frame term,
				Set<OWLAxiom> owlAxioms,
				List<Pair<Frame, Set<OWLAxiom>>> changedTermRelations,
				TermGenerationInput input,
				String error,
				List<String> warnings)
		{
			super();
			this.term = term;
			this.owlAxioms = owlAxioms;
			this.changedTermRelations = changedTermRelations;
			this.input = input;
			this.error = error;
			this.warnings = warnings;
		}

		/**
		 * @return the term
		 */
		public Frame getTerm() {
			return term;
		}
		
		/**
		 * @return the warnings
		 */
		public List<String> getWarnings() {
			return warnings;
		}
		
		/**
		 * @return the error
		 */
		public String getError() {
			return error;
		}

		/**
		 * @return the owlAxioms
		 */
		public Set<OWLAxiom> getOwlAxioms() {
			return owlAxioms;
		}

		/**
		 * @return the input
		 */
		public TermGenerationInput getInput() {
			return input;
		}

		/**
		 * @return the changedTermRelations
		 */
		public final List<Pair<Frame, Set<OWLAxiom>>> getChangedTermRelations() {
			return changedTermRelations;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TermGenerationOutput [");
			builder.append("success=");
			builder.append(error == null);
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
			if (error != null) {
				builder.append(", ");
				builder.append("error=");
				builder.append(error);
			}
			if (warnings != null) {
				builder.append(", ");
				builder.append("warnings=");
				builder.append(warnings);
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

		private final Map<String, List<String>> terms;
		private final Map<String, List<String>> strings;

		public TermGenerationParameters() {
			super();
			this.terms = new HashMap<String, List<String>>();
			this.strings = new HashMap<String, List<String>>();
		}

		/**
		 * @return the terms
		 */
		public Map<String, List<String>> getTerms() {
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
		public void setTermValues(String field, List<String> values) {
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
