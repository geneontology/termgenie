package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpServletRequest;

import org.openid4java.discovery.Identifier;


public interface OpenIdResponseHandler {

	public Identifier verifyResponse(HttpServletRequest httpReq);
}
