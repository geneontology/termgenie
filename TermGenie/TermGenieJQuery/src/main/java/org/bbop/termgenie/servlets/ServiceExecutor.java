package org.bbop.termgenie.servlets;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.GenerateTermsServiceImpl;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.OntologyServiceImpl;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.SessionHandlerImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermCommitServiceImpl;
import org.json.rpc.server.JsonRpcExecutor;
import org.json.rpc.server.JsonRpcServerTransport;

public class ServiceExecutor {

	private final JsonRpcExecutor executor;
	
	public ServiceExecutor() {
		executor = bind();
	}
	
	@SuppressWarnings("unchecked")
	private JsonRpcExecutor bind() {
        JsonRpcExecutor executor = new JsonRpcExecutor();

        GenerateTermsService generate = new GenerateTermsServiceImpl();
		executor.addHandler("generate", generate, GenerateTermsService.class);
		
		OntologyService ontology = new OntologyServiceImpl();
		executor.addHandler("ontology", ontology, OntologyService.class);

		TermCommitService commit = new TermCommitServiceImpl();
		executor.addHandler("commit", commit, TermCommitService.class);
		
		SessionHandler handler = new SessionHandlerImpl(); 
		executor.addHandler("user", handler, SessionHandler.class);
		
        return executor;
    }

	public void execute(JsonRpcServerTransport transport) {
		executor.execute(transport);
	}
	
}
