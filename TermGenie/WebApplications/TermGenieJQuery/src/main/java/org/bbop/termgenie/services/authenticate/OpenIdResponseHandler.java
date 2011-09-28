package org.bbop.termgenie.services.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface OpenIdResponseHandler {

	public UserData verifyResponse(HttpServletRequest httpReq, HttpSession session);
}
