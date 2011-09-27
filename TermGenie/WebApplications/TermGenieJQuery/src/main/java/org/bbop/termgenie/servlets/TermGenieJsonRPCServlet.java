package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.rpc.server.InjectingJsonRpcExecutor;
import org.json.rpc.server.JsonRpcServletTransport;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TermGenieJsonRPCServlet extends HttpServlet {

	// generated
	private static final long serialVersionUID = -3251117884213830675L;

	private final InjectingJsonRpcExecutor executor;
	
	@Inject
	public TermGenieJsonRPCServlet(InjectingJsonRpcExecutor executor)
	{
		super();
		this.executor = executor;
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		response.setContentType("text/html");
		response.getWriter().write("rpc service is running...");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		executor.execute(new JsonRpcServletTransport(request, response), request, response, getServletContext());
	}

}
