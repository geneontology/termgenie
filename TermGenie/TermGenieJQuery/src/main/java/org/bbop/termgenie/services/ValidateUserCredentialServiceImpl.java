package org.bbop.termgenie.services;

import org.bbop.termgenie.tools.ImplementationFactory;

public class ValidateUserCredentialServiceImpl implements ValidateUserCredentialService {

	@Override
	public boolean isValidUser(String username, String password) {
		return ImplementationFactory.getUserCredentialValidator().validate(username, password);
	}
}
