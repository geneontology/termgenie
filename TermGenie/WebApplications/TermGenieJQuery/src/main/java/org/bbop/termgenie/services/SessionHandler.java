package org.bbop.termgenie.services;

public interface SessionHandler {

	/**
	 * Default entry for a user name after a the session has been logged-in.
	 */
	public static final String USER_NAME = "TermGenieUserName";
	
	/**
	 * Create a new session.
	 * 
	 * @return sessionId
	 */
	public String createSession();

	/**
	 * Check if the session for the given sessionId is valid.
	 * 
	 * @param sessionId
	 * @return true, if the current session exists and has not timed out.
	 */
	public boolean isValidSession(String sessionId);
	
	/**
	 * Try to authenticate the session.
	 * 
	 * @param sessionId
	 * @param username
	 * @param password
	 * @return boolean, true if the login was succesful
	 */
	public boolean login(String sessionId, String username, String password);

	/**
	 * Invalidate the authentication of this session and associated data
	 * 
	 * @param sessionId
	 */
	public void logout(String sessionId);

	/**
	 * Check if the session for the given sessionId is authenticated.
	 * 
	 * @param sessionId
	 * @return true, if the current session exists and has not timed out.
	 */
	public boolean isAuthenticated(String sessionId);
	
	/**
	 * Ping the current session to keep it alive.
	 * 
	 * @param sessionId
	 * @return true if the keep aliva was successful
	 */
	public boolean keepSessionAlive(String sessionId);

	/**
	 * Retrieve a value for the given key in the specified session.
	 * 
	 * @param sessionId
	 * @param key
	 * @return String or null
	 */
	public String getValue(String sessionId, String key);

	/**
	 * Set a value for a given key in the specified session.
	 * 
	 * @param sessionId
	 * @param key
	 * @param value
	 * @return true, if operation was successful. May fail, if session timed out.
	 */
	public boolean setValue(String sessionId, String key, String value);

	/**
	 * Retrieve a list of values for a given keyset.
	 * 
	 * @param sessionId
	 * @param keys
	 * @return array of values, may contains null values
	 */
	public String[] getValues(String sessionId, String[] keys);

	/**
	 * Set a list of values for a given keyset.
	 * 
	 * @param sessionId
	 * @param keys
	 * @param values
	 * @return true, if operation was successful. May fail, if session timed out.
	 */
	public boolean setValues(String sessionId, String keys[], String[] values);

}
