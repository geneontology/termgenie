package org.bbop.termgenie.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractJsonRPCServlet extends HttpServlet {

	// generated
	private static final long serialVersionUID = -3251117884213830675L;

	private static volatile ServiceExecutor executor = null;

	public AbstractJsonRPCServlet() {
		super();
		synchronized (AbstractJsonRPCServlet.class) {
			if (executor == null) {
				executor = createServiceExecutor();
			}
		}
	}
	
	protected abstract ServiceExecutor createServiceExecutor();

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
		executor.execute(request, response);
	}

}
