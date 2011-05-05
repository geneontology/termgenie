package org.bbop.termgenie.server;

import org.bbop.termgenie.services.ValidateUserCredentialService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ValidateUserCredentialServiceImpl extends RemoteServiceServlet implements
		ValidateUserCredentialService {

	@Override
	public boolean isValidUser(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}

}
