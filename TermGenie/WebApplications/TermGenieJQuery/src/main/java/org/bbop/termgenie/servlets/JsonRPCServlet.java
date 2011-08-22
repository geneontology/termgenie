package org.bbop.termgenie.servlets;


public class JsonRPCServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = -3052651034871303985L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new DefaultServiceExecutor();
	}


}
