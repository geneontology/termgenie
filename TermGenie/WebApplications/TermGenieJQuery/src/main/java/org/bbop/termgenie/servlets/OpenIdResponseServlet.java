package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.authenticate.OpenIdHandler;
import org.bbop.termgenie.services.authenticate.UserData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class OpenIdResponseServlet extends HttpServlet {

	// generated
	private static final long serialVersionUID = 4776195215868823440L;

	private final OpenIdHandler openIdHandler;
	private final InternalSessionHandler sessionHandler;
	private final String defaultTermGenieUrl;

	@Inject
	public OpenIdResponseServlet(OpenIdHandler openIdHandler,
			InternalSessionHandler sessionHandler,
			@Named("DefaultTermGenieUrl") String defaultTermGenieUrl)
	{
		super();
		this.openIdHandler = openIdHandler;
		this.sessionHandler = sessionHandler;
		this.defaultTermGenieUrl = defaultTermGenieUrl;
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
		HttpSession session = req.getSession(false);
		if (session != null) {
			UserData userData = openIdHandler.verifyResponse(req, session);
			if (userData != null && userData.getError() == null) {
				sessionHandler.setAuthenticated(userData.getScreenname(),
						userData.getGuid(),
						session);
			}
		}
		resp.sendRedirect(defaultTermGenieUrl);
	}
}
