package org.bbop.termgenie.core.io;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;

/**
 * Tool for parsing a simple cardinality constraint.
 */
public class CardinalityHelper
{
	private final static String SINGLE_CONSTANT = "1"; 
	private final static String ONE2N_CONSTANT = "1..N";
	private final static String TWO2N_CONSTANT = "2..N";
	
	public static Cardinality parseCardinality(String string) {
		if (string == null || SINGLE_CONSTANT.equals(string)) {
			// do not write this out, assume this as default
			return TemplateField.SINGLE_FIELD_CARDINALITY;
		}
		else if (TWO2N_CONSTANT.equals(string)) {
			return TemplateField.TWO_TO_N_CARDINALITY;
		}
		else if (ONE2N_CONSTANT.equals(string)) {
			return TemplateField.ONE_TO_N_CARDINALITY;
		}
		throw new RuntimeException("Unkown cardinality: "+string);
	}
	
	public static String serializeCardinality(Cardinality cardinality) {
		if (TemplateField.SINGLE_FIELD_CARDINALITY.equals(cardinality)) {
			// do not write this info, assume this as default
			return null;
		}
		else if (TemplateField.TWO_TO_N_CARDINALITY.equals(cardinality)) {
			return TWO2N_CONSTANT;
		}
		else if (TemplateField.ONE_TO_N_CARDINALITY.equals(cardinality)) {
			return ONE2N_CONSTANT;
		}
		throw new RuntimeException("Unkown cardinality: "+cardinality);
	}
}