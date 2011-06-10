package org.bbop.termgenie.servlets;

import org.apache.log4j.Logger;
import org.bbop.termgenie.services.ValidateUserCredentialServiceImpl;

import lib.jsonrpc.RPCService;

public class ValidateUserCredentialServlet extends AbstractJsonRPCServlet {

	private static final long serialVersionUID = 8193554577728526317L;
	private static final Logger logger = Logger.getLogger(ValidateUserCredentialServlet.class);

	@Override
	protected RPCService createService() {
		return new ValidateUserCredentialServiceImpl();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
