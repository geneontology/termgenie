package org.bbop.termgenie.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("checkuser")
public interface ValidateUserCredentialService extends RemoteService {

	/**
	 * @param username
	 * @param password
	 * @return true, if the username and password are valid.
	 */
	public boolean isValidUser(String username, String password);
}
