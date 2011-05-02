package org.bbop.termgenie.core.io;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;

/**
 * Tool for parsing a simple cardinality constraint.
 */
public class CardinalityHelper
{
	private final static String SINGLE_CONSTANT = "1"; 
	private final static String TWO2N_CONSTANT = "2..N";
	
	public static Cardinality parseCardinality(String string) {
		if (SINGLE_CONSTANT.equals(string)) {
			return TemplateField.SINGLE_FIELD_CARDINALITY;
		}
		else if (TWO2N_CONSTANT.equals(string)) {
			return TemplateField.TWO_TO_N_CARDINALITY;
		}
		throw new RuntimeException("Unkown cardinality: "+string);
	}
	
	public static String serializeCardinality(Cardinality cardinality) {
		if (TemplateField.SINGLE_FIELD_CARDINALITY.equals(cardinality)) {
			return SINGLE_CONSTANT;
		}
		else if (TemplateField.TWO_TO_N_CARDINALITY.equals(cardinality)) {
			return TWO2N_CONSTANT;
		}
		throw new RuntimeException("Unkown cardinality: "+cardinality);
	}
}