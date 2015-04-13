package org.bbop.termgenie.servlets;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.servlets.TermLookupServlet.JsonLookupRequest;
import org.bbop.termgenie.tools.UrlTools;

import com.google.gson.Gson;


public class TermLookupServletMain {

	public static void main(String[] args) throws Exception {
		//URL url = new URL("http://localhost:8080/termgenie/termlookup");
		URL url = new URL("http://go.termgenie.org/termlookup");
		Gson gson = new Gson();
		JsonLookupRequest request = new JsonLookupRequest();
		request.id = "GO:1900040";
		request.action = "lookup";
		String json = gson.toJson(request);
		HttpURLConnection con = UrlTools.preparePost(url, json);
		InputStream response = null;
		String responseString = null;
		try {
			response = con.getInputStream();
			int status = con.getResponseCode();
			if (status != 200) {
				throw UrlTools.createStatusCodeException(status, con);
			}
			String charset = UrlTools.getCharset(con);
			if (charset != null) {
				responseString = IOUtils.toString(response, charset);
			}
			else {
				responseString = IOUtils.toString(response);
			}
		}
		finally {
			IOUtils.closeQuietly(response);
		}
		
		System.out.println(responseString);
	}
}
