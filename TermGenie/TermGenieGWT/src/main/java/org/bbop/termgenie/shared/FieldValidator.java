package org.bbop.termgenie.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.shared.GWTTermGenerationParameter.GWTMultiValueMap;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FieldValidator {

	public static class GWTValidationHint implements IsSerializable {

		public static final int FATAL = 15;
		public static final int ERROR = 5;
		public static final int WARN = 5;

		private GWTTemplateField field;
		private int level;
		private String hint;

		/**
		 * Default constructor required for serialization.
		 */
		private GWTValidationHint() {
			super();
		}

		/**
		 * @param field
		 * @param level
		 * @param hint
		 */
		public GWTValidationHint(GWTTemplateField field, int level, String hint) {
			this();
			this.field = field;
			this.level = level;
			this.hint = hint;
		}

		/**
		 * @param field
		 * @param hint
		 */
		public GWTValidationHint(GWTTemplateField field, String hint) {
			this(field, ERROR, hint);
		}

		/**
		 * @return the field
		 */
		public GWTTemplateField getField() {
			return field;
		}

		/**
		 * @param field
		 *            the field to set
		 */
		public void setField(GWTTemplateField field) {
			this.field = field;
		}

		/**
		 * @return the level
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @param level
		 *            the level to set
		 */
		public void setLevel(int level) {
			this.level = level;
		}

		/**
		 * @return the hint
		 */
		public String getHint() {
			return hint;
		}

		/**
		 * @param hint
		 *            the hint to set
		 */
		public void setHint(String hint) {
			this.hint = hint;
		}
	}

	public static List<GWTValidationHint> validateParameters(GWTTermTemplate template,
			GWTTermGenerationParameter parameter) {

		GWTTemplateField[] fields = template.getFields();
		List<GWTValidationHint> errors = new ArrayList<GWTValidationHint>();

		for (GWTTemplateField field : fields) {
			GWTCardinality cardinality = field.getCardinality();

			int count = parameter.getTerms().getCount(field);
			int stringCount = parameter.getStrings().getCount(field);
			GWTMultiValueMap<?> values = parameter.getTerms();

			if (count > 0 && stringCount > 0) {
				errors.add(new GWTValidationHint(field, "Conflicting values (string and ontology term) for field"));
			}
			if (stringCount > count) {
				count = stringCount;
				values = parameter.getStrings();
			}

			if (field.isRequired()) {
				// assert minimum
				if (count < cardinality.getMin()) {
					errors.add(new GWTValidationHint(field, "Minimum Cardinality not met."));
				}

				// assert maximum
				if (count > cardinality.getMax()) {
					errors.add(new GWTValidationHint(field, "Maximum Cardinality exceeded."));
				}

				// check fields for missing content
				for (int i = 0; i < count; i++) {
					Object value = values.getValue(field, i);
					if (value == null) {
						errors.add(new GWTValidationHint(field, "Required value missing."));
					}
				}

			}

			// check prefixes
			String[] defPrefixes = field.getFunctionalPrefixes();
			int prefixCount = parameter.getPrefixes().getCount(field);
			if (defPrefixes == null || defPrefixes.length == 0) {
				if (prefixCount > 0) {
					errors.add(new GWTValidationHint(field, "No prefixes expected."));
				}
			} else {
				if (prefixCount > 1) {
					errors.add(new GWTValidationHint(field, "Expected only one list of prefixes."));
				}
				List<String> prefixes = parameter.getPrefixes().getValue(field, 0);
				if (prefixes != null) {
					if (prefixes.size() > defPrefixes.length) {
						errors.add(new GWTValidationHint(field,
								"Expected only a list of prefixes of max length: "
										+ defPrefixes.length));
					}
					Set<String> defSetPrefixes = new HashSet<String>(Arrays.asList(defPrefixes));
					for (String prefix : prefixes) {
						if (!defSetPrefixes.contains(prefix)) {
							errors.add(new GWTValidationHint(field, "Unknow prefix: " + prefix));
						}
					}
				}
			}
		}
		return errors;
	}

}
