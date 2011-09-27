package org.bbop.termgenie.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.rpc.server.ServletAware;
import org.json.rpc.server.SessionAware;

public interface SessionHandler {

	/**
	 * Create a new session.
	 * 
	 * @param request introduced via {@link ServletAware}, do not send in rpc
	 *            call.
	 * @param response introduced via {@link ServletAware}, do not send in rpc
	 *            call.
	 * @return sessionId
	 */
	@ServletAware
	public String createSession(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Check if the session for the given sessionId is valid.
	 * 
	 * @param sessionId
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return true, if the current session exists and has not timed out.
	 */
	@SessionAware
	public boolean isValidSession(String sessionId, HttpSession session);

	/**
	 * Try to authenticate the session.
	 * 
	 * @param sessionId
	 * @param username
	 * @param password
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return boolean, true if the login was successful
	 */
	@SessionAware
	public boolean login(String sessionId, String username, String password, HttpSession session);

	/**
	 * Invalidate the authentication of this session and associated data
	 * 
	 * @param sessionId
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 */
	@SessionAware
	public void logout(String sessionId, HttpSession session);

	/**
	 * Check if the session for the given sessionId is authenticated.
	 * 
	 * @param sessionId
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return screenname, if the current session exists and has not timed out and
	 *         is authenticated, otherwise null.
	 */
	@SessionAware
	public String isAuthenticated(String sessionId, HttpSession session);

	/**
	 * Ping the current session to keep it alive.
	 * 
	 * @param sessionId
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return true if the keep alive was successful
	 */
	@SessionAware
	public boolean keepSessionAlive(String sessionId, HttpSession session);

	/**
	 * Retrieve a value for the given key in the specified session.
	 * 
	 * @param sessionId
	 * @param key
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return String or null
	 */
	@SessionAware
	public String getValue(String sessionId, String key, HttpSession session);

	/**
	 * Set a value for a given key in the specified session.
	 * 
	 * @param sessionId
	 * @param key
	 * @param value
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return true, if operation was successful. May fail, if session timed
	 *         out.
	 */
	@SessionAware
	public boolean setValue(String sessionId, String key, String value, HttpSession session);

	/**
	 * Retrieve a list of values for a given keyset.
	 * 
	 * @param sessionId
	 * @param keys
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return array of values, may contains null values
	 */
	@SessionAware
	public String[] getValues(String sessionId, String[] keys, HttpSession session);

	/**
	 * Set a list of values for a given keyset.
	 * 
	 * @param sessionId
	 * @param keys
	 * @param values
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return true, if operation was successful. May fail, if session timed
	 *         out.
	 */
	@SessionAware
	public boolean setValues(String sessionId, String keys[], String[] values, HttpSession session);

}
