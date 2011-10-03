package org.bbop.termgenie.services.authenticate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.UrlIdentifier;
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

	private final static Logger logger = Logger.getLogger(OpenIdHandlerImpl.class);

	private final ConsumerManager manager;
	private final String returnToUrl;

	@Inject
	public OpenIdHandlerImpl(@Named("OpenIdHandlerReturnToUrl") String returnToUrl) {
		super();
		this.returnToUrl = returnToUrl;
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
			fetch.addAttribute("guid", "http://openid.net/schema/person/guid", true);
			fetch.addAttribute("screenname", "http://openid.net/schema/namePerson/friendly", true);

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
	public UserData verifyResponse(HttpServletRequest httpReq, HttpSession session) {
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
				String guid = null;
				String screenname = null;
				AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

					guid = fetchResp.getAttributeValue("guid");
					screenname = fetchResp.getAttributeValue("screenname");
				}

				if (guid == null) {
					guid = verified.getIdentifier();
				}
				if (screenname == null) {
					if (verified instanceof UrlIdentifier) {
						UrlIdentifier urlIdentifier = (UrlIdentifier) verified;
						String path = urlIdentifier.getUrl().getPath();
						if (path != null && path.length() > 1) {
							if (path.charAt(0) == '/') {
								path = path.substring(1);
							}
							screenname = path;
						}
					}
					if (screenname == null) {
						// may look ugly but is correct
						screenname = verified.getIdentifier();
					}
				}
				UserData userData = new UserData(guid, screenname);
				logger.info("Successful openId login: " + userData);
				return userData;
			}
		} catch (Exception exception) {
			logger.error("Internal error during OpenId check stage 2.", exception);
		}
		return null;
	}

}
