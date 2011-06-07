package org.bbop.termgenie.servlets;

import org.bbop.termgenie.rpc.JSONMethod;
import org.bbop.termgenie.rpc.JsonRpcServlet;

@SuppressWarnings("serial")
public class ValidateUserCredentialServlet extends JsonRpcServlet {

	@JSONMethod
	public boolean isValidUser(String username, String password) {
		return UserCredentialValidator.getInstance().validate(username, password);
	}

	/**
	 * Actual implementation of the validation.
	 */
	public static class UserCredentialValidator {
		
		private static volatile UserCredentialValidator instance = null;
		
		public static synchronized UserCredentialValidator getInstance() {
			if (instance == null) {
				instance = new UserCredentialValidator();
			}
			return instance;
		}
		
		private UserCredentialValidator() {
			super();
		}
		
		
		public boolean validate(String username, String password) {
			// TODO add real implementation: web service call?
			return false;
		}
	}
}
