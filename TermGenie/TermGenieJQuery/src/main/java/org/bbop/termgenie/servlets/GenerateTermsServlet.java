package org.bbop.termgenie.servlets;

import lib.jsonrpc.RPCService;

import org.apache.log4j.Logger;
import org.bbop.termgenie.services.GenerateTermsServiceImpl;

public class GenerateTermsServlet extends AbstractJsonRPCServlet {

	private static final long serialVersionUID = -3488888136933653464L;
	private static final Logger logger = Logger.getLogger(GenerateTermsServlet.class);

	@Override
	protected RPCService createService() {
		return new GenerateTermsServiceImpl();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
