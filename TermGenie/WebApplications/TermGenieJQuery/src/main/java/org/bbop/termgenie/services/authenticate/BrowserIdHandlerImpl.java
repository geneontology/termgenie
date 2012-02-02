package org.bbop.termgenie.services.authenticate;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.services.InternalSessionHandler;
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
	
	private final DefaultHttpClient client = new DefaultHttpClient();
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
		HttpPost post = new HttpPost(browserIdVerificationUrl);
		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
			pairs.add(new BasicNameValuePair("assertion", assertion));
			String host = req.getHeader(HTTP.TARGET_HOST);
			// host is only defined in HTTP 1.1 not in 1.0
			// in most cases this should work, but keep the configuration as fall-back
			String audienceValue = host != null ? host : termgenieBrowserIdAudience;
			pairs.add(new BasicNameValuePair("audience", audienceValue));
			post.setEntity(new UrlEncodedFormEntity(pairs));

			HttpResponse response;
			synchronized (client) {
				response = client.execute(post);
			}
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				String json = EntityUtils.toString(entity);
				BrowserIdVerificationResponse details = gson.fromJson(json,
						BrowserIdVerificationResponse.class);
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
			}
			return new JsonUserData("BrowserID could not be verified.");
		} catch (Exception exception) {
			logger.warn("Could not verify browserId", exception);
			return new JsonUserData("Internal error during BrowserID verification: " + exception.getMessage());
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
