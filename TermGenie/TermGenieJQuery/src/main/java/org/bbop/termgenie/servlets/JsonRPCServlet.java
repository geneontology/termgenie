package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.rpc.server.JsonRpcServletTransport;

public class JsonRPCServlet extends HttpServlet {

	private static final long serialVersionUID = -3251117884213830675L;

	private final ServiceExecutor executor;
	
	public JsonRPCServlet() {
		super();
		executor = ServiceExecutor.getInstance();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		response.getWriter().write("rpc service is running...");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	    executor.execute(new JsonRpcServletTransport(request, response));
	}

}
