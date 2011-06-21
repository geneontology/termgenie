package org.bbop.termgenie.services;


public interface TermCommitService {

	/**
	 * @param username
	 * @param password
	 * @param ontology
	 * @return true, if the username and password are valid.
	 */
	public boolean isValidUser(String username, String password, String ontology);
}
