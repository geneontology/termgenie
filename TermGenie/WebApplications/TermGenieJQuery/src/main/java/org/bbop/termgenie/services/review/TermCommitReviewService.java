package org.bbop.termgenie.services.review;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;
import org.json.rpc.server.ProcessStateAware;
import org.json.rpc.server.SessionAware;

public interface TermCommitReviewService {

	/**
	 * Retrieve the current service configuration. For example whether this service is enabled.
	 * 
	 * @return config
	 */
	public JsonReviewConfig getConfig();
	
	/**
	 * Check the current session, for an authenticated user and check its
	 * permissions.
	 * 
	 * @param sessionId
	 * @param session
	 * @return true, if there is an authenticated user with the correct
	 *         permissions.
	 */
	@SessionAware
	public boolean isAuthorized(String sessionId, HttpSession session);

	/**
	 * Retrieve the entries waiting to be reviewed.
	 * 
	 * @param sessionId
	 * @param session
	 * @return array of entries for review
	 */
	@SessionAware
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session);

	/**
	 * Commit the given list changes.
	 * 
	 * @param sessionId
	 * @param entries
	 * @param session
	 * @param state
	 * @return commitResult
	 */
	@SessionAware
	@ProcessStateAware
	public JsonCommitReviewCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session,
			ProcessState state);
}
