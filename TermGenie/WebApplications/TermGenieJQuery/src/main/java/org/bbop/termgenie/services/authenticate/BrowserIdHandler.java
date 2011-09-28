package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpSession;

import org.json.rpc.server.SessionAware;


/**
 * Methods associated with the authentication via BrowserId (https://browserid.org/)
 */
public interface BrowserIdHandler {

	/**
	 * Verify the assertion for the given session.
	 * 
	 * @param sessionId
	 * @param assertion
	 * @param httpSession
	 * @return {@link UserData} or null
	 */
	@SessionAware
	public UserData verifyAssertion(String sessionId, String assertion, HttpSession httpSession);
}
