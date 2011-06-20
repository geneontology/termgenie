package org.bbop.termgenie.server;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;

/**
 * Helper class for generating informative responses for a given list of
 * generation results. TODO this is only used for mockups during development.
 */
public class TermGenerationMessageTool {

	/**
	 * Generate message for a given candidate, without the optional step of
	 * committing the new terms to the ontology.
	 * 
	 * @param candidate
	 * @return message for one candidate
	 */
	public static String generateTermValidationMessage(TermGenerationOutput candidate) {
		StringBuilder sb = new StringBuilder();
		TermGenerationInput input = candidate.getInput();
		sb.append("For the following template: ");
		TermTemplate template = input.getTermTemplate();
		sb.append(template.getName());
		sb.append('\n');
		sb.append("and the parameters: ");
		appendParameters(sb, input, template);
		sb.append('\n');
		String message = candidate.getMessage();
		if (message != null) {
			sb.append("Details: ");
			sb.append(message);
		}
		OntologyTerm term = candidate.getTerm();
		if (term != null) {
			sb.append('\n');
			appendTermDetails(sb, term);
		}
		return sb.toString();
	}

	static void appendTermDetails(StringBuilder builder, OntologyTerm term) {
		// TODO: Think about using obo format here, for later copy and paste by the
		// user.
		builder.append("Term: ").append('\n');
		if (term.getId() != null)
			builder.append("  Id:       ").append(term.getId()).append('\n');
		if (term.getLabel() != null)
			builder.append("  Label:    ").append(term.getLabel()).append('\n');
		if (term.getDefinition() != null)
			builder.append("  Def:      ").append(term.getDefinition()).append('\n');
		if (term.getSynonyms() != null)
			builder.append("  Synonyms: ").append(term.getSynonyms()).append('\n');
	}

	static void appendParameters(StringBuilder sb, TermGenerationInput input, TermTemplate template) {
		TermGenerationParameters parameters = input.getParameters();
		List<TemplateField> fields = template.getFields();
		for (int i = 0; i < fields.size(); i++) {
			TemplateField field = fields.get(i);
			Cardinality cardinality = field.getCardinality();
			if (cardinality.getMaximum() == 1) {
				// single value
				if (field.hasCorrespondingOntologies()) {
					// term
					OntologyTerm ontologyTerm = parameters.getTerms()[i][0];
					if (ontologyTerm != null) {
						sb.append(field.getName());
						sb.append(" : ");
						sb.append(ontologyTerm.getLabel());
						sb.append(' ');
						String[] prefixes = parameters.getStrings()[i];
						if (prefixes != null) {
							sb.append(Arrays.toString(prefixes));
							sb.append(' ');
						}
					}
				} else {
					// string
					String value = parameters.getStrings()[i][0];
					if (value != null) {
						sb.append(field.getName());
						sb.append(" : ");
						sb.append(value);
						sb.append(' ');
					}
				}
			} else {
				// list value
				int count = parameters.getTerms()[i].length;
				if (count > 0) {
					sb.append(field.getName());
					sb.append(" : [");
					for (int j = 0; j < count; j++) {
						OntologyTerm ontologyTerm = parameters.getTerms()[i][j];
						if (j > 0) {
							sb.append(',');
						}
						sb.append(ontologyTerm.getLabel());
					}
					sb.append("] ");
				}
				count = parameters.getStrings()[i].length;
				if (count > 0) {
					sb.append(field.getName());
					sb.append(" : ");
					sb.append(Arrays.toString(parameters.getStrings()[i]));
				}
			}
		}
	}
}
