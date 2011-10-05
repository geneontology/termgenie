package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.rpc.server.ServletAware;
import org.json.rpc.server.SessionAware;

/**
 * Methods associated with the authentication via BrowserId
 * (https://browserid.org/)
 */
public interface BrowserIdHandler {

	/**
	 * Verify the assertion for the given session.
	 * 
	 * @param sessionId
	 * @param assertion
	 * @param req 
	 * @param resp 
	 * @param httpSession
	 * @return {@link UserData} or null
	 */
	@ServletAware
	@SessionAware
	public UserData verifyAssertion(String sessionId,
			String assertion,
			HttpServletRequest req,
			HttpServletResponse resp,
			HttpSession httpSession);
}
