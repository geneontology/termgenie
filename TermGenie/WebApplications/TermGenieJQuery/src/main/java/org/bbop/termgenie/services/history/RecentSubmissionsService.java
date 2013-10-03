package org.bbop.termgenie.services.history;

import javax.servlet.http.HttpSession;

import org.json.rpc.server.SessionAware;


public interface RecentSubmissionsService {

	/**
	 * Check if this service is enabled.
	 * 
	 * @return true, if this service is active
	 */
	public boolean isEnabled();

	/**
	 * Check the current session, for an authenticated user and check its
	 * permissions to view the recent commits
	 * 
	 * @param sessionId
	 * @param session
	 * @return true, if there is an authenticated user with the correct
	 *         permissions.
	 */
	@SessionAware
	public boolean canView(String sessionId, HttpSession session);
	
	
	/**
	 * Retrieve the recently submitted terms.
	 * 
	 * @param sessionId
	 * @param session
	 * @return array of entries for review
	 */
	@SessionAware
	public JsonRecentSubmission[] getRecentTerms(String sessionId, HttpSession session);

}
