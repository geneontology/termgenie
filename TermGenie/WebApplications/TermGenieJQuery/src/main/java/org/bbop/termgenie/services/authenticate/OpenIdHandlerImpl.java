package org.bbop.termgenie.services.authenticate;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class OpenIdHandlerImpl implements OpenIdHandler {

	private final ConsumerManager manager;
	private final String returnToUrl;

	@Inject
	public OpenIdHandlerImpl(@Named("OpenIdHandlerReturnToUrl") String returnToUrl) {
		super();
		this.returnToUrl = returnToUrl;
		manager = new ConsumerManager();
	}

	@Override
	public String authRequest(String userSuppliedString,
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			ServletContext servletContext) throws IOException, OpenIDException, ServletException
	{
		// perform discovery on the user-supplied identifier
		List<?> discoveries = manager.discover(userSuppliedString);

		// attempt to associate with the OpenID provider
		// and retrieve one service endpoint for authentication
		DiscoveryInformation discovered = manager.associate(discoveries);

		// store the discovery information in the user's session
		httpReq.getSession().setAttribute("openid-disc", discovered);

		// obtain a AuthRequest message to be sent to the OpenID provider
		AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

		if (!discovered.isVersion2()) {
			// Option 1: GET HTTP-redirect to the OpenID Provider endpoint
			// The only method supported in OpenID 1.x
			// redirect-URL usually limited ~2048 bytes
			httpResp.sendRedirect(authReq.getDestinationUrl(true));
			return null;
		}
		// Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
		RequestDispatcher dispatcher = servletContext.getRequestDispatcher("formredirection.jsp");
		httpReq.setAttribute("parameterMap", authReq.getParameterMap());
		httpReq.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
		dispatcher.forward(httpReq, httpResp);
		return null;
	}

	// --- processing the authentication response ---
	@Override
	public Identifier verifyResponse(HttpServletRequest httpReq) {
		try {
			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(httpReq.getParameterMap());

			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute("openid-disc");

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
			return verified;
		} catch (OpenIDException e) {
			// present error to the user
		}
		return null;
	}

}
