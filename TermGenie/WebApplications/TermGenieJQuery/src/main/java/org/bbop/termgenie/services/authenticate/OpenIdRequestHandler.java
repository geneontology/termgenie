package org.bbop.termgenie.services.authenticate;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.rpc.server.ServletAware;
import org.json.rpc.server.SessionAware;

public interface OpenIdRequestHandler {

	@SessionAware
	@ServletAware
	public RedirectRequest authRequest(String sessionId,
			String userSuppliedString,
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			HttpSession session);

	public static class RedirectRequest {

		private String url = null;
		private String[][] parameters = null;
		private String error = null;

		public RedirectRequest() {
			super();
		}

		public static RedirectRequest createError(String error) {
			RedirectRequest request = new RedirectRequest();
			request.error = error;
			return request;
		}
		
		public RedirectRequest(String url) {
			this.url = url;
			this.parameters = null;
		}

		public RedirectRequest(String url, Map<?, ?> parameters) {
			this.url = url;
			this.parameters = null;
			if (parameters != null && !parameters.isEmpty()) {
				this.parameters = new String[parameters.size()][];
				int count = 0;
				for(Entry<?, ?> entry : parameters.entrySet()) {
					Object key = entry.getKey();
					Object value = entry.getValue();
					if (key != null && value != null) {
						this.parameters[count] = new String[] { key.toString(), value.toString() };
					}
					count += 1;
				}
				if (count < this.parameters.length) {
					this.parameters = Arrays.copyOf(this.parameters, count);
				}
			}
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @return the parameters
		 */
		public String[][] getParameters() {
			return parameters;
		}

		/**
		 * @param parameters the parameters to set
		 */
		public void setParameters(String[][] parameters) {
			this.parameters = parameters;
		}

		/**
		 * @return the error
		 */
		public String getError() {
			return error;
		}

		/**
		 * @param error the error to set
		 */
		public void setError(String error) {
			this.error = error;
		}
	}

}
