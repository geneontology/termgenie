package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import org.json.rpc.server.SessionAware;

/**
 * Methods for retrieving information, which can be used in auto-completion
 */
public interface AutoCompleteResources {

	/**
	 * Retrieve the name value pairs for a given auto-complete resource.
	 * 
	 * @param sessionId
	 * @param resource
	 * @param session
	 * @return array of entries or null
	 */
	@SessionAware
	public AutoCompleteEntry[] getAutoCompleteResource(String sessionId, String resource, HttpSession session);
	
	public static class AutoCompleteEntry {
		
		private String name;
		private String value;
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
		
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
	}
}
