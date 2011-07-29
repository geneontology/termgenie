package org.bbop.termgenie.tools;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Actual implementation of the validation.
 */
@Singleton
public class UserCredentialValidatorTools {
	
	@Inject
	UserCredentialValidatorTools() {
		super();
	}
	
	/**
	 * @param username
	 * @param password
	 * @param ontology
	 * @return true, iff the credentials are valid.
	 */
	public boolean validate(String username, String password, String ontology) {
		// TODO add real implementation: web service call?
		return false;
	}
}