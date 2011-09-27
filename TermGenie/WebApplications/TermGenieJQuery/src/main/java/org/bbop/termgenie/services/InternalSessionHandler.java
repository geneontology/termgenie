package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

/**
 * Separate methods that should not be exposed via an RPC call.
 */
public interface InternalSessionHandler extends SessionHandler {

	/**
	 * Set the current session as authenticated for the given user name.
	 * 
	 * @param screenname
	 * @param guid
	 * @param session
	 */
	public void setAuthenticated(String screenname, String guid, HttpSession session);
}
