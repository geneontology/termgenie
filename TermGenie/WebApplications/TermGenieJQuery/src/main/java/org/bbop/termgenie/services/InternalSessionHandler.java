package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.user.UserData;

/**
 * Separate methods that should not be exposed via an RPC call.
 */
public interface InternalSessionHandler extends SessionHandler {

	/**
	 * Set the current session as authenticated for the given user name.
	 * 
	 * @param userData
	 * @param session
	 */
	public void setAuthenticated(UserData userData, HttpSession session);

	/**
	 * Retrieve the GUID for an authenticated session.
	 * 
	 * @param session
	 * @return userData or null
	 */
	public UserData getUserData(HttpSession session);
}
