package org.bbop.termgenie.server;

import org.bbop.termgenie.services.ValidateUserCredentialService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ValidateUserCredentialServiceImpl extends RemoteServiceServlet implements
		ValidateUserCredentialService {

	@Override
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
