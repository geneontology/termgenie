package org.bbop.termgenie.services.authenticate;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataProvider;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class OpenIdHandlerImpl implements OpenIdHandler {

	private static final String EMAIL_OPEN_ID_URI = "http://schema.openid.net/contact/email";
	private static final String GUID_OPEN_ID_URI = "http://openid.net/schema/person/guid";
	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String GUID_ATTRIBUTE = "guid";

	private final static Logger logger = Logger.getLogger(OpenIdHandlerImpl.class);

	private final ConsumerManager manager;
	private final String returnToUrl;
	private final UserDataProvider userDataProvider;

	@Inject
	public OpenIdHandlerImpl(@Named("OpenIdHandlerReturnToUrl") String returnToUrl,
			UserDataProvider userDataProvider) {
		super();
		this.returnToUrl = returnToUrl;
		this.userDataProvider = userDataProvider;
		logger.info("Configuring OpenID: OpenIdHandlerReturnToUrl="+returnToUrl);
		try {
			manager = new ConsumerManager();
		} catch (ConsumerException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public RedirectRequest authRequest(String sessionId,
			String userSuppliedString,
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			HttpSession session)
	{
		if (session == null) {
			logger.warn("OpenId check failed: session is null.");
			return RedirectRequest.createError("OpenId check failed: session is null.");
		}
		try {
			// perform discovery on the user-supplied identifier
			List<?> discoveries = manager.discover(userSuppliedString);

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session
			session.setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

			// Attribute Exchange example: fetching the 'email' attribute
			FetchRequest fetch = FetchRequest.createFetchRequest();
			fetch.addAttribute(GUID_ATTRIBUTE, GUID_OPEN_ID_URI, true);
			fetch.addAttribute(EMAIL_ATTRIBUTE, EMAIL_OPEN_ID_URI, true);  

			// attach the extension to the authentication request
			authReq.addExtension(fetch);

			if (!discovered.isVersion2()) {
				return new RedirectRequest(authReq.getDestinationUrl(false));
			}
			return new RedirectRequest(authReq.getDestinationUrl(false), authReq.getParameterMap());
		} catch (Exception exception) {
			logger.error("Internal error during OpenId check stage 1.", exception);
			return RedirectRequest.createError("OpenId check failed: " + exception.getMessage());
		}
	}

	// --- processing the authentication response ---
	@Override
	public boolean verifyResponse(HttpServletRequest httpReq, HttpSession session, InternalSessionHandler sessionHandler) {
		try {
			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(httpReq.getParameterMap());

			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("openid-disc");

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0) receivingURL.append("?").append(httpReq.getQueryString());

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(receivingURL.toString(),
					response,
					discovered);

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();

			if (verified != null) {
				List<String> emails = null;
				String guid = null;
				AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

					guid = fetchResp.getAttributeValue(GUID_ATTRIBUTE);
					List<?> emailCandidates = fetchResp.getAttributeValues(EMAIL_ATTRIBUTE);
                    if (emailCandidates != null && !emailCandidates.isEmpty()) {
                    	emails = new ArrayList<String>(emailCandidates.size());
						for (Object object : emailCandidates) {
							if (object instanceof String) {
								emails.add((String) object);
							}
						}
					}
				}
				if (guid == null) {
					guid = verified.getIdentifier();
				}
				if (emails == null || emails.isEmpty()) {
					logger.warn("Successful authentication aborted for "+guid+"\nReason: missing attribute e-mail");
					return false;
				}
				UserData userData = userDataProvider.getUserDataPerGuid(guid, emails);
				sessionHandler.setAuthenticated(userData, session);
				logger.info("Successful openId login: " + userData.getGuid());
				return true;
			}
		} catch (Exception exception) {
			logger.error("Internal error during OpenId check stage 2.", exception);
		}
		return false;
	}

}
