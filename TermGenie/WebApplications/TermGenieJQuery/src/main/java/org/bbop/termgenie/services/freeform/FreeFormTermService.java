package org.bbop.termgenie.services.freeform;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;
import org.json.rpc.server.ProcessStateAware;
import org.json.rpc.server.SessionAware;

public interface FreeFormTermService {

	/**
	 * Check if this service is enabled.
	 * 
	 * @return true, if this service is active
	 */
	public boolean isEnabled();

	/**
	 * Check the current session, for an authenticated user and check its
	 * permissions to view the free form template
	 * 
	 * @param sessionId
	 * @param session
	 * @return true, if there is an authenticated user with the correct
	 *         permissions.
	 */
	@SessionAware
	public boolean canView(String sessionId, HttpSession session);
	
	/**
	 * Validate the given free form term requests.
	 * 
	 * @param sessionId
	 * @param request
	 * @param session
	 * @param state
	 * @return validation result
	 */
	@SessionAware
	@ProcessStateAware
	public JsonFreeFormValidationResponse validate(String sessionId,
			JsonFreeFormTermRequest request,
			HttpSession session,
			ProcessState state);
}
