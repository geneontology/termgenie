package org.bbop.termgenie.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.tools.TermGenieToolsModule;
import org.json.rpc.server.JsonRpcExecutor;
import org.json.rpc.server.JsonRpcServerTransport;

import com.google.inject.Injector;

/**
 * Tool for registering the implementations for the TermGenie web application.
 */
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

	private IOCModule[] getConfiguration() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		modules.add(new TermGenieToolsModule());
		modules.add(new TermGenieServiceModule());
		modules.add(getOntologyModule());
		modules.add(getReasoningModule());
		modules.add(getRulesModule());
		Collection<IOCModule> additionalModules = getAdditionalModules();
		if (additionalModules != null && !additionalModules.isEmpty()) {
			modules.addAll(additionalModules);
		}
		return modules.toArray(new IOCModule[modules.size()]);
	}

	/**
	 * @return module handling loading of ontologies
	 */
	protected abstract IOCModule getOntologyModule();

	/**
	 * @return module providing the rule engine and configuration of patterns
	 */
	protected abstract IOCModule getRulesModule();

	/**
	 * @return module providing the implementations of the reasoner
	 */
	protected IOCModule getReasoningModule() {
		return new ReasonerModule();
	}

	/**
	 * @return null or additional modules required for the start-up
	 */
	protected Collection<IOCModule> getAdditionalModules() {
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> void add(String path, Injector injector, Class<T> c) {
		executor.addHandler(path, injector.getInstance(c), c);
	}

	public void execute(JsonRpcServerTransport transport) {
		executor.execute(transport);
	}

}
