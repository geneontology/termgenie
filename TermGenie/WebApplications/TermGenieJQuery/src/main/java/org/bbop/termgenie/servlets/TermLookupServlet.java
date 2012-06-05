package org.bbop.termgenie.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.bbop.termgenie.services.lookup.TermLookupService;
import org.bbop.termgenie.services.lookup.TermLookupService.LookupCallBack;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TermLookupServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(TermLookupServlet.class);

	// generated
	private static final long serialVersionUID = 4604786454943166862L;
	
	private final TermLookupService lookupService;
	private final Gson gson;

	@Inject
	public TermLookupServlet(TermLookupService lookupService) {
		super();
		this.lookupService = lookupService;
		gson = new Gson();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.setContentType("text/html");
		response.getWriter().write("/termlookup service is running...");
	}

	@Override
	protected void doPost(HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException
	{
		String json = readRequest(req);
		final JsonLookupRequest request = gson.fromJson(json, JsonLookupRequest.class);
		final JsonLookupResponse jsonResponse = new JsonLookupResponse();
		jsonResponse.request = request;
		LookupCallBack callback = new LookupCallBack() {
			
			@Override
			public void unknown(String id) {
				jsonResponse.status = "unknown";
				sendResponse(jsonResponse, resp);
			}
			
			@Override
			public void regular(OWLGraphWrapper graph, OWLClass cls, String id) {
				jsonResponse.status = "good";
				jsonResponse.name = graph.getLabel(cls);
				sendResponse(jsonResponse, resp);
			}
			
			@Override
			public void pending(String id, String label) {
				jsonResponse.status = "in review";
				jsonResponse.name = label;
				sendResponse(jsonResponse, resp);
			}
			
			@Override
			public void error(String message, Exception e) {
				sendError(message, e, resp);
			}
		};
		lookupService.lookup(request.id, callback);
	}
	
	private void sendResponse(JsonLookupResponse jsonResponse, final HttpServletResponse resp) {
		byte[] data = gson.toJson(jsonResponse).getBytes();
		resp.addHeader("Content-Type", "application/json");
		resp.setHeader("Content-Length", Integer.toString(data.length));

		OutputStream out = null;
		try {
			out = resp.getOutputStream();
			out.write(data);
			out.flush();
		} catch (IOException exception) {
			logger.error("Could not write response for term lookup, status: "+jsonResponse.status, exception);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException exception) {
					logger.warn("Could not close output stream for response to term lookup.", exception);
				}
			}
		}

	}
	
	private void sendError(String message, Exception e, HttpServletResponse resp) {
		try {
			logger.warn("Error during term lookup: "+message, e);
			resp.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, message);
		} catch (IOException exception) {
			logger.error("Could not send error message response.", exception);
		}
	}
	
	private String readRequest(HttpServletRequest req) throws IOException {
		InputStream in = null;
        try {
            in = req.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buff = new byte[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                bos.write(buff, 0, n);
            }

            return bos.toString();
        } finally {
            if (in != null) {
                in.close();
            }
        }
	}
	
	static class JsonLookupRequest {
		
		String action;
		String id;
	}
	
	
	static class JsonLookupResponse {
		
		JsonLookupRequest request;
		String status;
		String name;
	}

}
