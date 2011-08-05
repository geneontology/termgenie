package org.bbop.termgenie.servlets;

import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.rules.DefaultDynamicRulesModule;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.tools.TermGenieToolsModule;
import org.json.rpc.server.JsonRpcExecutor;
import org.json.rpc.server.JsonRpcServerTransport;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ServiceExecutor {

	private static volatile ServiceExecutor instance = null;
	
	private final JsonRpcExecutor executor;
	
	public static synchronized ServiceExecutor getInstance() {
		if (instance == null) {
			instance = new ServiceExecutor();
		}
		return instance;
	}
	
	private ServiceExecutor() {
		executor  = new JsonRpcExecutor();
        Injector injector = Guice.createInjector(
        		new DefaultOntologyModule(),
        		new DefaultDynamicRulesModule(),
        		new TermGenieToolsModule(),
        		new TermGenieServiceModule());

        add("generate", injector, GenerateTermsService.class);
        add("ontology", injector, OntologyService.class);
        add("commit", injector, TermCommitService.class);
        add("user", injector, SessionHandler.class);
		
    }
	
	@SuppressWarnings("unchecked")
	private <T> void add(String path, Injector injector, Class<T> c) {
		executor.addHandler(path, injector.getInstance(c), c);
	}

	public void execute(JsonRpcServerTransport transport) {
		executor.execute(transport);
	}
	
}
