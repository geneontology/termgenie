package org.bbop.termgenie.services;


public interface ValidateUserCredentialService {

	/**
	 * @param username
	 * @param password
	 * @return true, if the username and password are valid.
	 */
	public boolean isValidUser(String username, String password);
}
