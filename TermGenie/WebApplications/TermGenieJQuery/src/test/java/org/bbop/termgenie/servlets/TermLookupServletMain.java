package org.bbop.termgenie.servlets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bbop.termgenie.servlets.TermLookupServlet.JsonLookupRequest;

import com.google.gson.Gson;


public class TermLookupServletMain {

	public static void main(String[] args) throws Exception {
		HttpClient client = new DefaultHttpClient();
		String url = "http://localhost:8080/termgenie/termlookup";
		//String url = "http://go.termgenie.org/termlookup";
		HttpPost post = new HttpPost(url);
		Gson gson = new Gson();
		JsonLookupRequest request = new JsonLookupRequest();
		request.id = "GO:1900040";
		request.action = "lookup";
		HttpEntity reqEntity = new StringEntity(gson.toJson(request));
		post.setEntity(reqEntity);
		
		HttpResponse response = client.execute(post);
		HttpEntity entity = response.getEntity();
		String string = EntityUtils.toString(entity);
		
		System.out.println(string);
	}
}
