package org.bbop.termgenie.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.data.JsonTermGenerationParameter;
import org.bbop.termgenie.data.JsonTermTemplate;
import org.bbop.termgenie.data.JsonValidationHint;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonMultiValueMap;
import org.bbop.termgenie.data.JsonTermTemplate.JsonCardinality;
import org.bbop.termgenie.data.JsonTermTemplate.JsonTemplateField;

public class FieldValidatorTool {

	public static List<JsonValidationHint> validateParameters(JsonTermTemplate template,
			JsonTermGenerationParameter parameter) {

		JsonTemplateField[] fields = template.getFields();
		List<JsonValidationHint> errors = new ArrayList<JsonValidationHint>();

		for (JsonTemplateField field : fields) {
			JsonCardinality cardinality = field.getCardinality();

			int count = parameter.getTerms().getCount(field);
			int stringCount = parameter.getStrings().getCount(field);
			JsonMultiValueMap<?> values = parameter.getTerms();

			if (count > 0 && stringCount > 0) {
				errors.add(new JsonValidationHint(template, field, "Conflicting values (string and ontology term) for field"));
			}
			if (stringCount > count) {
				count = stringCount;
				values = parameter.getStrings();
			}

			if (field.isRequired()) {
				// assert minimum
				if (count < cardinality.getMin()) {
					errors.add(new JsonValidationHint(template, field, "Minimum Cardinality not met."));
				}

				// assert maximum
				if (count > cardinality.getMax()) {
					errors.add(new JsonValidationHint(template, field, "Maximum Cardinality exceeded."));
				}

				// check fields for missing content
				for (int i = 0; i < count; i++) {
					Object value = values.getValue(field, i);
					if (value == null) {
						errors.add(new JsonValidationHint(template, field, "Required value missing."));
					}
				}

			}

			// check prefixes
			String[] defPrefixes = field.getFunctionalPrefixes();
			int prefixCount = parameter.getPrefixes().getCount(field);
			if (defPrefixes == null || defPrefixes.length == 0) {
				if (prefixCount > 0) {
					errors.add(new JsonValidationHint(template, field, "No prefixes expected."));
				}
			} else {
				if (prefixCount > 1) {
					errors.add(new JsonValidationHint(template, field, "Expected only one list of prefixes."));
				}
				List<String> prefixes = parameter.getPrefixes().getValue(field, 0);
				if (prefixes != null) {
					if (prefixes.size() > defPrefixes.length) {
						errors.add(new JsonValidationHint(template, field,
								"Expected only a list of prefixes of max length: "
										+ defPrefixes.length));
					}
					Set<String> defSetPrefixes = new HashSet<String>(Arrays.asList(defPrefixes));
					for (String prefix : prefixes) {
						if (!defSetPrefixes.contains(prefix)) {
							errors.add(new JsonValidationHint(template, field, "Unknow prefix: " + prefix));
						}
					}
				}
			}
		}
		return errors;
	}

}
