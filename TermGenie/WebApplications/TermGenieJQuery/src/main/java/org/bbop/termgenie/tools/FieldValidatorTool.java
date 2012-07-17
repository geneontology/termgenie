package org.bbop.termgenie.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology;
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
			JsonTermGenerationParameter parameter)
	{

		List<TemplateField> fields = template.getFields();
		List<JsonValidationHint> errors = new ArrayList<JsonValidationHint>();
		Map<String, List<JsonOntologyTermIdentifier>> allTerms = parameter.getTerms();
		Map<String, List<String>> allStrings = parameter.getStrings();
		
		for (int i = 0; i < fields.size(); i++) {
			TemplateField field = fields.get(i);
			Cardinality cardinality = field.getCardinality();

			
			List<JsonOntologyTermIdentifier> fieldTerms = allTerms.get(field.getName());
			List<String> fieldStrings = null;
			if (allStrings != null) {
				fieldStrings = allStrings.get(field.getName());
			}

			int termCount = fieldTerms == null ? 0 : fieldTerms.size();
			int stringCount = fieldStrings == null ? 0 : fieldStrings.size();

			final boolean isRequired = field.isRequired();
			if (isRequired && termCount == 0 && stringCount == 0) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Required value missing."));
				continue;
			}

			final boolean hasOntologies = hasOntologies(field);
			if (hasOntologies && fieldStrings != null && stringCount > 0) {
				if (hasPrefixes(field)) {
					// check if strings correspond to the given prefixes in the
					// template
					Set<String> prefixes = new HashSet<String>(field.getFunctionalPrefixes());
					Set<String> ids = new HashSet<String>(field.getFunctionalPrefixesIds());
					for (String string : fieldStrings) {
						if (prefixes.contains(string) == false && ids.contains(string) == false) {
							errors.add(new JsonValidationHint(jsonTemplate, i, "Unknown prefix or Id: " + string));
						}
					}
				}
				else {
					errors.add(new JsonValidationHint(jsonTemplate, i, "Conflicting values (string and ontology term) for field"));
				}
			}

			int count = hasOntologies ? termCount : stringCount;

			// assert minimum
			if (isRequired && count < cardinality.getMinimum()) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Minimum Cardinality not met."));
			}

			// assert maximum
			if (count > cardinality.getMaximum()) {
				errors.add(new JsonValidationHint(jsonTemplate, i, "Maximum Cardinality exceeded."));
			}

			// check fields for missing content
			if (isRequired) {
				List<?> values = hasOntologies ? fieldTerms : fieldStrings;
				int realObjects = 0;
				if (values != null && values.size() > 0) {
					for (Object value : values) {
						if (value == null) {
							errors.add(new JsonValidationHint(jsonTemplate, i, "Required value missing."));
						}
						else {
							realObjects++;
						}
					}
				}
				if (realObjects == 0) {
					errors.add(new JsonValidationHint(jsonTemplate, i, "Required value missing."));
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
