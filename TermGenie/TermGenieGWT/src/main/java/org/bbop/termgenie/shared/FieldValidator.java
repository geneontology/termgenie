package org.bbop.termgenie.shared;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.shared.GWTTermGenerationParameter.OntologyTerm;
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
			if (field.isRequired()) {
				GWTCardinality cardinality = field.getCardinality();
				int count = parameter.getCount(field);
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
					if (field.getOntology() != null) {
						OntologyTerm term = parameter.getOntologyTerm(field, i);
						if (term == null) {
							errors.add(new GWTValidationHint(field,
									"Required ontology term missing."));
						}
					} else {
						if (parameter.getStringValue(field, i) == null) {
							errors.add(new GWTValidationHint(field,
									"Required String parameter missing."));
						}
					}
				}
			}
		}
		return errors;
	}

}
