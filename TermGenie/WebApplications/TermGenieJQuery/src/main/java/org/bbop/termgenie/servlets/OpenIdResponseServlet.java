package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.authenticate.OpenIdHandler;
import org.bbop.termgenie.services.authenticate.OpenIdResponseHandler.UserData;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OpenIdResponseServlet extends HttpServlet {

	private final static Logger logger = Logger.getLogger(OpenIdResponseServlet.class);
	
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
		logger.info("Processing OpenId response");
		HttpSession session = req.getSession(false);
		if (session != null) {
			UserData userData = openIdHandler.verifyResponse(req, session);
			if (userData != null) {
				logger.info("Successful authentication via OpenId.");
				sessionHandler.setAuthenticated(userData.getScreenname(), userData.getGuid(), session);
			}
		}
		resp.sendRedirect("http://localhost:8080/termgenie/");
	}
}
