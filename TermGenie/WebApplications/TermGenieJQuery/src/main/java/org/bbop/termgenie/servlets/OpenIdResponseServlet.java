package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.authenticate.OpenIdHandler;
import org.openid4java.discovery.Identifier;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OpenIdResponseServlet extends HttpServlet {

	// generated
	private static final long serialVersionUID = 4776195215868823440L;

	private final OpenIdHandler openIdHandler;
	private final InternalSessionHandler sessionHandler;

	@Inject
	public OpenIdResponseServlet(OpenIdHandler openIdHandler, InternalSessionHandler sessionHandler) {
		super();
		this.openIdHandler = openIdHandler;
		this.sessionHandler = sessionHandler;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		handle(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		handle(req, resp);
	}

	private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Identifier identifier = openIdHandler.verifyResponse(req);
		if (identifier != null) {
			HttpSession session = req.getSession(false);
			if (session != null) {
				String username = identifier.getIdentifier();
				sessionHandler.setAuthenticated(username, session);
			}
		}
		resp.sendRedirect("../");
	}
}
