package org.bbop.termgenie.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * https://developer.github.com/v3/oauth/#1-redirect-users-to-request-github-access
 */
@Singleton
public class GHAuthenticationRequestServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GHAuthenticationRequestServlet.class);

	// generated
	private static final long serialVersionUID = 4604786454943166862L;

	private final String clientId ;

	@Inject
	public GHAuthenticationRequestServlet(
			@Named("github_client_id")
                String clientId
	) {
		super();
		this.clientId = clientId ;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
//		String clientId = ConfigurationHandler.getConfigurationHandler().getValue("client_id");
		String url="https://github.com/login/oauth/authorize?client_id="+clientId+"&scope=user:email";
		resp.sendRedirect(url);
	}
	

}
