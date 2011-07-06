package org.bbop.termgenie.services;

public interface SessionHandler {

	/**
	 * Create a new session.
	 * 
	 * @return sessionId
	 */
	public String createSession();
	
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
	 */
	public void setValue(String sessionId, String key, String value);
	
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
	 */
	public void setValues(String sessionId, String keys[], String[] values);
}
