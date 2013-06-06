package org.bbop.termgenie.rules.impl;

import org.apache.commons.lang3.StringUtils;


/**
 * Tools for the validation of textual definition.
 */
public class TextualDefinitionTool {
	
	/**
	 * Validate a textual definition.
	 * 
	 * @param definition
	 * @return null or an error String
	 */
	public static String validateDefinition(String definition) {
		
		// Minimum length
		if (definition == null) {
			return "A definition is required.";
		}
		if (StringUtils.trimToNull(definition) == null) {
			return "A definition should not be empty.";
		}
		if (definition.length() < 10) {
			return "The definition is too short.";
		}
		
		// Should not start with a whitespace
		if (Character.isWhitespace(definition.charAt(0))) {
			return "The defintion should not start with a whitepace.";
		}
		
		int length = definition.length();
		char lastChar = definition.charAt(length - 1);
		
		// Should not end with a whitespace
		if (Character.isWhitespace(lastChar)) {
			return "The definition should not end with a whitespace.";
		}
		
		// ends with a full stop
		if (lastChar != '.') {
			return "A definition must end in with a full stop.";
		}
		
		// multiple whitespace check
		char prev = ' ';
		for (int i = 0; i < length; i++) {
			char c = definition.charAt(i);
			if (i > 0) {
				boolean isWhitespace = Character.isWhitespace(c);
				if (isWhitespace && Character.isWhitespace(prev)) {
					return "The definition contains consecutive whitespaces at position: "+i;
				}
				if (isWhitespace && c != ' ') {
					return "The defintion contains an unexpected type of spacer character at position: "+i;
				}
			}
			prev = c;
		}
		
		// spell check?
		
		return null;
	}
	
}
