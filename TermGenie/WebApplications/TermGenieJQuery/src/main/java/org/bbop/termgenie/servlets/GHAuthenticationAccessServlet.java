package org.bbop.termgenie.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.bbop.termgenie.services.info.ConfigurationHandler;
import org.bbop.termgenie.services.lookup.TermLookupService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

//	private final TermLookupService lookupService;
//	private final Gson gson;

	@Inject
	public GHAuthenticationAccessServlet() {
		super();
//		this.lookupService = lookupService;
//		gson = new Gson();
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{

		String requestString = req.toString();
		String code = null ;


		String accessToken = getAccessToken(code);

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet getRequest = new HttpGet("https://api.github.com/user?access_token="+accessToken);
		HttpResponse response = httpClient.execute(getRequest);

//		def jsonObject = (new JsonSlurper()).parse(url) as JSONObject
//		// is authenticated:
//		boolean isAuthenticated = jsonObject.containsKey("email")
		

//		UserData userData  = userDataProvider.getUserDataPerEMail(details.email);
//		sessionHandler.setAuthenticated(userData, httpSession);
	}

	private String getAccessToken(String code) throws IOException{
		// we have to pull the returned "code" off of the server
		// and then do a post to github to get the access_code
		String clientId= ConfigurationHandler.getConfigurationHandler().getValue("client_id");
		String clientSecret = ConfigurationHandler.getConfigurationHandler().getValue("github.client_secret");

		// https://developer.github.com/v3/oauth/#2-github-redirects-back-to-your-site
		// TODO: 1 post to the client to get the acces token
//		def parameterMap = [
//		client_id: clientToken
//				,client_secret : clientSecret
//			, code: code
//        ]

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("client_id",clientId));
		urlParameters.add(new BasicNameValuePair("client_secret",clientSecret));
//		parameterMap.each {
//		urlParameters.add(new BasicNameValuePair(it.key,it.value))
//	}

		HttpClient httpClient = HttpClientBuilder.create().build();
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

		String resultString = result.toString();


//		def jsonSlurper = new JsonSlurper()
//		JSONObject jsonObject = jsonSlurper.parseText(result.toString()) as JSONObject
//		String accessToken = jsonObject.access_token
//		return accessToken

		String accessToken = "";
		return accessToken ;
	}


}
