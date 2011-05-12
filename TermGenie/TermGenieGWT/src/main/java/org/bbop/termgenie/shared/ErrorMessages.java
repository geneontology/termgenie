package org.bbop.termgenie.shared;

/**
 * Standardized error messages for the application.
 * 
 * TODO: decide if this should be done in a property file (externalize strings).
 */
public class ErrorMessages {

	public static final String MISSING_USERNAME = "Please specify a user name, other wise a commit is not possible.";
	public static final String UNKOWN_USERNAME_PASSWORD = "Unable to verify the specified user name and password.";
	public static final String NO_ONTOLOGY = "No ontology specified in your request";
	public static final String NO_TERM_GENERATION_PARAMETERS = "No term generation parameters were found in your request";
	public static final String NO_TERMS_GENERATED = "No terms could be generated from your request";
	public static final String UNEXPECTED_NULL_VALUE = "Unexpected null value.";
}
