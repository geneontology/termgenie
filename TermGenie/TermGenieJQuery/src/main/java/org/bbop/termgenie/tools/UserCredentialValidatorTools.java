package org.bbop.termgenie.tools;

/**
 * Actual implementation of the validation.
 */
public class UserCredentialValidatorTools {
	
	private static volatile UserCredentialValidatorTools instance = null;
	
	public static synchronized UserCredentialValidatorTools getInstance() {
		if (instance == null) {
			instance = new UserCredentialValidatorTools();
		}
		return instance;
	}
	
	private UserCredentialValidatorTools() {
		super();
	}
	
	
	public boolean validate(String username, String password) {
		// TODO add real implementation: web service call?
		return false;
	}
}