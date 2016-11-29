package org.bbop.termgenie.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * https://developer.github.com/v3/oauth/#2-github-redirects-back-to-your-site
 */
@Singleton
public class GHAuthenticationAccessServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GHAuthenticationAccessServlet.class);

	// generated
	private static final long serialVersionUID = 4604786454943166862L;

	private final Gson gson;
	private final UserDataProvider userDataProvider;
	private final InternalSessionHandler sessionHandler;
	private final String clientId ;
	private final String clientSecret ;

	@Inject
	public GHAuthenticationAccessServlet(
			InternalSessionHandler sessionHandler,
			UserDataProvider userDataProvider,
			@Named("github_client_id")
			String clientId,
			@Named("github_client_secret")
			String clientSecret
	) {
		super();
//		this.lookupService = lookupService;
		this.gson = new Gson();
		this.userDataProvider = userDataProvider;
		this.sessionHandler = sessionHandler;
		this.clientId = clientId ;
		this.clientSecret = clientSecret;
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		String code = req.getParameter("code");

		String accessToken = getAccessToken(code);

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet("https://api.github.com/user?access_token="+accessToken);
		HttpResponse response = httpClient.execute(getRequest);

		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));

		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		GHUserResponse ghUserResponse = gson.fromJson(result.toString(), GHUserResponse.class);

		boolean isAuthenticated = ghUserResponse.email!=null;

		if(isAuthenticated){
			UserData userData  = userDataProvider.getUserDataPerEMail(ghUserResponse.email);
			HttpSession httpSession = req.getSession();
			sessionHandler.setAuthenticated(userData, httpSession);
		}
		else{
			throw new RuntimeException("Failed to authenticate");
		}
	}

	private String getAccessToken(String code) throws IOException{
		// we have to pull the returned "code" off of the server
		// and then do a post to github to get the access_code
//		String clientId= ConfigurationHandler.getConfigurationHandler().getValue("client_id");
//		String clientSecret = ConfigurationHandler.getConfigurationHandler().getValue("github.client_secret");

		// https://developer.github.com/v3/oauth/#2-github-redirects-back-to-your-site
		// TODO: 1 post to the client to get the acces token

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("client_id",this.clientId));
		urlParameters.add(new BasicNameValuePair("client_secret",this.clientSecret));
		urlParameters.add(new BasicNameValuePair("code",code));

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost("https://github.com/login/oauth/access_token");
		postRequest.addHeader("User-Agent", "TermGenie/1.0");
		postRequest.addHeader("Accept","application/json");
		postRequest.addHeader("Accept","application/xml");

		System.out.println("posting '${urlParameters}'");

		postRequest.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse response = httpClient.execute(postRequest);

		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		GHAccessResponse ghAccessResponse = gson.fromJson(result.toString(), GHAccessResponse.class);

		String accessToken = ghAccessResponse.access_token;
		return accessToken ;
	}

	static class GHUserResponse {
		String email;
		String username;
	}

	static class GHAccessResponse {
		String access_token;
	}

}
