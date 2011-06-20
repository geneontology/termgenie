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

public class GWTFieldValidator {

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

			int termcount = parameter.getTerms().getCount(field);
			int stringCount = parameter.getStrings().getCount(field);
			
			boolean isRequired = field.isRequired();
			boolean hasOntology = field.hasOntologies();

			if (isRequired && termcount == 0 && stringCount == 0) {
				errors.add(new GWTValidationHint(field, "Required value missing."));
				continue;
			}
			if (hasOntology && stringCount > 0) {
				if (hasPrefixes(field)) {
					Set<String> allPrefixes = new HashSet<String>(Arrays.asList(field.getFunctionalPrefixes()));
					for(int i = 0; i < stringCount; i++) {
						String prefix = parameter.getStrings().getValue(field, i);
						if (!allPrefixes.contains(prefix)) {
							errors.add(new GWTValidationHint(field, "Unknow prefix: " + prefix));
						}
					}
				}
			}
			int count = hasOntology ? termcount : stringCount;

			// assert minimum
			if (isRequired && count < cardinality.getMin()) {
				errors.add(new GWTValidationHint(field, "Minimum Cardinality not met."));
			}

			// assert maximum
			if (count > cardinality.getMax()) {
				errors.add(new GWTValidationHint(field, "Maximum Cardinality exceeded."));
			}
			
			if (isRequired) {
				// check fields for missing content
				GWTMultiValueMap<?> values = hasOntology ? parameter.getTerms() : parameter.getStrings();
				for (int i = 0; i < termcount; i++) {
					Object value = values.getValue(field, i);
					if (value == null) {
						errors.add(new GWTValidationHint(field, "Required value missing."));
					}
				}
			}
		}
		return errors;
	}

	private static boolean hasPrefixes(GWTTemplateField field) {
		String[] prefixes = field.getFunctionalPrefixes();
		if (prefixes != null) {
			return prefixes.length > 0;
		}
		return false;
	}
	
}
