package org.bbop.termgenie.services.freeform;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.services.AutoCompleteResources;
import org.json.rpc.server.ProcessStateAware;
import org.json.rpc.server.SessionAware;

public interface FreeFormTermService extends AutoCompleteResources {

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
	 * Get the available OBO namespaces for the free form template.
	 * 
	 * @param sessionId
	 * @param session
	 * @return array of OBO namespaces
	 */
	@SessionAware
	public String[] getAvailableNamespaces(String sessionId, HttpSession session);
	
	/**
	 * Auto complete the query with terms. If available search only in the given
	 * OBO namespace. Return only max number of results.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param query
	 * @param oboNamespace
	 * @param max
	 * @return term suggestions
	 */
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String oboNamespace,
			int max);
	
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
	
	/**
	 * Submit a free form term for review. Retrieve the username and
	 * password from the session
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param term
	 * @param sendConfirmationEMail
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @param processState introduced via {@link ProcessStateAware}, do not send
	 *            in rpc call.
	 * @return {@link JsonCommitResult}
	 */
	@SessionAware
	@ProcessStateAware
	public JsonCommitResult submit(String sessionId,
			JsonOntologyTerm term,
			boolean sendConfirmationEMail,
			HttpSession session,
			ProcessState processState);
}
