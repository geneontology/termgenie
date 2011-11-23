package org.bbop.termgenie.services.management;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.rpc.server.IOCInjectorAware;
import org.json.rpc.server.SessionAware;

import com.google.inject.Injector;


public interface ManagementServices {

	/**
	 * Check the current session, for an authenticated user and check its
	 * permissions.
	 * 
	 * @param sessionId
	 * @param session automatically injected by framework
	 * @return true, if there is an authenticated user with the correct
	 *         permissions.
	 */
	@SessionAware
	public boolean isAuthorized(String sessionId, HttpSession session);
	
	/**
	 * Retrieve the current configuration of TermGenie.
	 * 
	 * @param sessionId
	 * @param session automatically injected by framework
	 * @param injector 
	 * @return list of module details
	 */
	@IOCInjectorAware
	@SessionAware
	public List<JsonModuleConfigDetails> getModuleDetails(String sessionId, HttpSession session, Injector injector);
	
	
	/**
	 * Retrieve the system details of TermGenie.
	 * 
	 * @param sessionId
	 * @param session automatically injected by framework
	 * @return systemDetails
	 */
	@SessionAware
	public JsonSystemDetails getSystemDetails(String sessionId, HttpSession session);
	
	/**
	 * Create a thread dump of TermGenie.
	 * 
	 * @param sessionId
	 * @param session automatically injected by framework
	 * @return list of thread details
	 */
	@SessionAware
	public List<String> getThreadDump(String sessionId, HttpSession session);
	
	/**
	 * Create an overview of the sessions in the TermGenie web application.
	 * 
	 * @param sessionId
	 * @param session automatically injected by framework
	 * @return session details
	 */
	@SessionAware
	public JsonSessionDetails getSessionDetails(String sessionId, HttpSession session);
}
