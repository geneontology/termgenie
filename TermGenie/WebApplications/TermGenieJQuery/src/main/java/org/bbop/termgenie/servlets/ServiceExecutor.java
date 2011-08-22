package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.json.rpc.server.JsonRpcExecutor;
import org.json.rpc.server.JsonRpcServerTransport;

import com.google.inject.Injector;

public abstract class ServiceExecutor {

	private final JsonRpcExecutor executor;

	protected ServiceExecutor() {
		super();
		executor = new JsonRpcExecutor();
		Injector injector = TermGenieGuice.createInjector(getConfiguration());

		add("generate", injector, GenerateTermsService.class);
		add("ontology", injector, OntologyService.class);
		add("commit", injector, TermCommitService.class);
		add("user", injector, SessionHandler.class);
	}

	protected abstract IOCModule[] getConfiguration();
	
	@SuppressWarnings("unchecked")
	private <T> void add(String path, Injector injector, Class<T> c) {
		executor.addHandler(path, injector.getInstance(c), c);
	}

	public void execute(JsonRpcServerTransport transport) {
		executor.execute(transport);
	}

}
