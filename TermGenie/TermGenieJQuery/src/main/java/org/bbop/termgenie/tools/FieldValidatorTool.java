package org.bbop.termgenie.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.data.JsonTermGenerationParameter;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermTemplate;
import org.bbop.termgenie.data.JsonValidationHint;

public class FieldValidatorTool {

	public static List<JsonValidationHint> validateParameters(TermTemplate template,
			JsonTermTemplate jsonTemplate,
			JsonTermGenerationParameter parameter) {

		List<TemplateField> fields = template.getFields();
		List<JsonValidationHint> errors = new ArrayList<JsonValidationHint>();

		for (int i = 0; i < fields.size(); i++) {
			TemplateField field = fields.get(i);
			Cardinality cardinality = field.getCardinality();

			JsonOntologyTermIdentifier[] terms = parameter.getTermLists()[i];
			String[] strings = parameter.getStringLists()[i];
			
			int termCount = terms == null ? 0 : terms.length;
			int stringCount = strings == null ? 0 : strings.length;

			final boolean isRequired = field.isRequired();
			if (isRequired && termCount == 0 && stringCount == 0) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Required value missing."));
				continue;
			}
			
			final boolean hasOntologies = hasOntologies(field);
			if (hasOntologies && stringCount > 0) {
				if (hasPrefixes(field)) {
					// check if strings correspond to the given prefixes in the template
					Set<String> prefixes = new HashSet<String>(field.getFunctionalPrefixes());
					for(String string : strings) {
						if (!prefixes.contains(string)) {
							errors.add(new JsonValidationHint(jsonTemplate, i, "Unknown prefix: "+string));
						}
					}
				}
				errors.add(new JsonValidationHint(jsonTemplate, i, "Conflicting values (string and ontology term) for field"));
			}
			
			int count = hasOntologies ? termCount : stringCount;

			// assert minimum
			if (isRequired && count < cardinality.getMinimum()) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Minimum Cardinality not met."));
			}

			// assert maximum
			if (termCount > cardinality.getMaximum()) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Maximum Cardinality exceeded."));
			}

			// check fields for missing content
			if (isRequired) {
				Object[] values = hasOntologies ? terms : strings;
				for (Object value : values) {
					if (value == null) {
						errors.add(new JsonValidationHint(jsonTemplate, i, "Required value missing."));
					}
				}
			}
		}
		return errors;
	}

	private static boolean hasOntologies(TemplateField field) {
		List<Ontology> ontologies = field.getCorrespondingOntologies();
		return ontologies != null && !ontologies.isEmpty();
	}
	
	private static boolean hasPrefixes(TemplateField field) {
		List<String> prefixes = field.getFunctionalPrefixes();
		return prefixes != null && !prefixes.isEmpty();
	}
	
}
