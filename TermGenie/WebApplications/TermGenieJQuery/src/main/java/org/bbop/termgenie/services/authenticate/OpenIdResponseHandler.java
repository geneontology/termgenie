package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface OpenIdResponseHandler {

	public UserData verifyResponse(HttpServletRequest httpReq, HttpSession session);
	
	public static class UserData {
		
		private final String screenname;
		private final String guid;
		
		public UserData(String guid, String screenname) {
			this.guid = guid;
			this.screenname = screenname;
		}

		/**
		 * @return the screenname
		 */
		public String getScreenname() {
			return screenname;
		}

		/**
		 * @return the guid
		 */
		public String getGuid() {
			return guid;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("UserData [screenname=");
			builder.append(screenname);
			builder.append(", guid=");
			builder.append(guid);
			builder.append("]");
			return builder.toString();
		}
	}
}
