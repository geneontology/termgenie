package org.bbop.termgenie.services.authenticate;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.rpc.server.ServletAware;
import org.json.rpc.server.ServletContextAware;
import org.openid4java.OpenIDException;

public interface OpenIdRequestHandler {

	@ServletAware
	@ServletContextAware
	public String authRequest(String userSuppliedString,
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			ServletContext servletContext) throws IOException, OpenIDException, ServletException;

}
