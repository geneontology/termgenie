package org.bbop.termgenie.services;

import org.bbop.termgenie.tools.ImplementationFactory;

import lib.jsonrpc.BasicRPCService;

public class ValidateUserCredentialServiceImpl extends BasicRPCService implements ValidateUserCredentialService {

	@Override
	public boolean isValidUser(String username, String password) {
		return ImplementationFactory.getUserCredentialValidator().validate(username, password);
	}
}
