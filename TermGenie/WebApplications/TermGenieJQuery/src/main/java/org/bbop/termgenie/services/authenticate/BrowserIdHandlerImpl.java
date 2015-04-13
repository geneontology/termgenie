package org.bbop.termgenie.services.authenticate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.tools.UrlTools;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataProvider;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class BrowserIdHandlerImpl implements BrowserIdHandler {

	private static final Logger logger = Logger.getLogger(BrowserIdHandlerImpl.class);

	private static final Gson gson = new Gson();
	
	private final String browserIdVerificationUrl;
	private final String termgenieBrowserIdAudience;
	private final InternalSessionHandler sessionHandler;
	private final UserDataProvider userDataProvider;

	@Inject
	public BrowserIdHandlerImpl(@Named("BrowserIdVerificationUrl") String browserIdVerificationUrl,
			@Named("TermGenieBrowserIdAudience") String termgenieBrowserIdAudience,
			InternalSessionHandler sessionHandler,
			UserDataProvider userDataProvider)
	{
		super();
		this.browserIdVerificationUrl = browserIdVerificationUrl;
		this.termgenieBrowserIdAudience = termgenieBrowserIdAudience;
		this.sessionHandler = sessionHandler;
		this.userDataProvider = userDataProvider;
		logger.info("Configuring BrowserID: verificationURL=" + browserIdVerificationUrl + " termgenieBrowserIdAudience=" + termgenieBrowserIdAudience);
	}

	@Override
	public JsonUserData verifyAssertion(String sessionId,
			String assertion,
			HttpServletRequest req,
			HttpServletResponse resp,
			HttpSession httpSession)
	{
		try {
			String host = req.getHeader("Host");
			// host is only defined in HTTP 1.1 not in 1.0
			// in most cases this should work, but keep the configuration as fall-back
			String audienceValue = host != null ? host : termgenieBrowserIdAudience;
			
			String json = postRequest(browserIdVerificationUrl, assertion, audienceValue);
			
			BrowserIdVerificationResponse details = gson.fromJson(json, BrowserIdVerificationResponse.class);
			if (details.status != null) {
				if ("okay".equals(details.status)) {
					if (details.email != null) {
						// TODO verify 'details.issuer' and
						// 'details.validUntil'
						UserData userData  = userDataProvider.getUserDataPerEMail(details.email);
						sessionHandler.setAuthenticated(userData, httpSession);
						return new JsonUserData(userData);
					}
				}
				else {
					return new JsonUserData("BrowserID could not be verified status: " + details.status + " reason: " + details.reason);
				}
			}
			return new JsonUserData("BrowserID could not be verified.");
		} catch (Exception exception) {
			logger.warn("Could not verify browserId", exception);
			return new JsonUserData("Internal error during BrowserID verification: " + exception.getMessage());
		}
	}
	
	private String postRequest(String urlString, String assertion, String audience) throws IOException {
		// URL
		URL url = new URL(urlString);
		
		// POST data
		String data = "assertion=" + URLEncoder.encode(assertion, "UTF-8") +"&"
					+ "audience=" + URLEncoder.encode(audience, "UTF-8");

		// prepare connection
		HttpURLConnection con = UrlTools.preparePost(url, data);
		
		// execute post, by opening the input stream
		InputStream response = null;
		try {
			response = con.getInputStream();
			int status = con.getResponseCode();
			if (status != 200) {
				throw UrlTools.createStatusCodeException(status, con);
			}
			String charset = UrlTools.getCharset(con);
			String responseString;
			if (charset != null) {
				responseString = IOUtils.toString(response, charset);
			}
			else {
				responseString = IOUtils.toString(response);
			}
			return responseString;
		}
		finally {
			IOUtils.closeQuietly(response);
		}
	}

	@SuppressWarnings("unused")
	private static class BrowserIdVerificationResponse {

		String status;
		String reason;
		String email;
		String audience;
		@SerializedName("valid-until")
		Long validUntil;
		String issuer;
	}
}
