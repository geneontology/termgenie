package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bbop.termgenie.services.InternalSessionHandler;

public interface OpenIdResponseHandler {

	public boolean verifyResponse(HttpServletRequest httpReq, HttpSession session, InternalSessionHandler sessionHandler);
}
