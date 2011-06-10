package org.bbop.termgenie.servlets;

import lib.jsonrpc.RPCService;

import org.apache.log4j.Logger;
import org.bbop.termgenie.services.OntologyServiceImpl;

public class OntologyServlet extends AbstractJsonRPCServlet {

	private static final long serialVersionUID = 7936433244200131046L;
	private static final Logger logger = Logger.getLogger(OntologyServlet.class);

	@Override
	protected RPCService createService() {
		return new OntologyServiceImpl();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}


}
