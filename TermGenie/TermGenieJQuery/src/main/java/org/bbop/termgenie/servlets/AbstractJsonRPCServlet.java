package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import lib.jsonrpc.RPCService;

public abstract class AbstractJsonRPCServlet extends HttpServlet {

	private static final long serialVersionUID = -3251117884213830675L;

	private final RPCService service;
	
	public AbstractJsonRPCServlet() {
		super();
		service = createService();
	}
	
	protected abstract RPCService createService();
	
	protected abstract Logger getLogger();
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html");
		response.getWriter().write("rpc service " + service.getServiceName() +  " is running...");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
		    service.dispatch(request, response);
		} catch (Throwable t) {
		    getLogger().warn(t.getMessage(), t);
		}
	}

}
