package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.authenticate.AuthenticationModule;
import org.bbop.termgenie.services.authenticate.BrowserIdHandler;
import org.bbop.termgenie.services.authenticate.OpenIdRequestHandler;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.bbop.termgenie.tools.TermGenieToolsModule;
import org.json.rpc.server.InjectingJsonRpcExecutor;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public abstract class AbstractTermGenieContextListener extends GuiceServletContextListener {

	protected final class TermGenieServletModule extends ServletModule {

		public static final String OPENID_SERVLET_PATH = "/openid";

		@Override
		protected void configureServlets() {
			serve("/jsonrpc").with(TermGenieJsonRPCServlet.class);
			serve(OPENID_SERVLET_PATH).with(OpenIdResponseServlet.class);
		}

		@Provides
		@Singleton
		@SuppressWarnings("unchecked")
		protected InjectingJsonRpcExecutor providesInjectingJsonRpcExecutor(GenerateTermsService generate,
				OntologyService ontology,
				TermCommitService commit,
				SessionHandler user,
				OpenIdRequestHandler openId,
				BrowserIdHandler browserId,
				TermCommitReviewService review)
		{
			InjectingJsonRpcExecutor executor = new InjectingJsonRpcExecutor();
			executor.addHandler("generate", generate, GenerateTermsService.class);
			executor.addHandler("ontology", ontology, OntologyService.class);
			executor.addHandler("commit", commit, TermCommitService.class);
			executor.addHandler("user", user, SessionHandler.class);
			executor.addHandler("openid", openId, OpenIdRequestHandler.class);
			executor.addHandler("browserid", browserId, BrowserIdHandler.class);
			executor.addHandler("review", review, TermCommitReviewService.class);

			return executor;
		}
	}

	protected final Properties applicationProperties;
	
	
	protected AbstractTermGenieContextListener(String applicationPropertyConfigName) {
		applicationProperties = new Properties();
		applicationProperties.isEmpty();
		String property = IOCModule.getSystemProperty(applicationPropertyConfigName);
		if (property != null) {
			File propertyFile = new File(property);
			if (propertyFile.isFile() && propertyFile.canRead()) {
				try {
					applicationProperties.load(new FileInputStream(propertyFile));
				} catch (FileNotFoundException exception) {
					throw new RuntimeException(exception);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		}
	}
	
	protected AbstractTermGenieContextListener(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Override
	protected Injector getInjector() {
		ServletModule module = new TermGenieServletModule();
		return TermGenieGuice.createWebInjector(module, applicationProperties, getConfiguration());
	}

	private IOCModule[] getConfiguration() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		modules.add(new TermGenieToolsModule(applicationProperties));
		add(modules, getServiceModule(), true, "ServiceModule");
		add(modules, getAuthenticationModule(), true, "Authentication");
		add(modules, getUserPermissionModule(), true, "UserPermission");
		add(modules, getOntologyModule(), true, "OntologyModule");
		add(modules, getReasoningModule(), true, "ReasoningModule");
		add(modules, getRulesModule(), true, "RulesModule");
		add(modules, getCommitModule(), false, "CommitModule");
		add(modules, getCommitReviewWebModule(), true, "CommitReviewModule");
		Collection<IOCModule> additionalModules = getAdditionalModules();
		if (additionalModules != null && !additionalModules.isEmpty()) {
			for (IOCModule module : additionalModules) {
				if (module != null) {
					modules.add(module);
				}
			}
		}
		return modules.toArray(new IOCModule[modules.size()]);
	}

	private void add(List<IOCModule> modules, IOCModule module, boolean required, String moduleName)
	{
		if (module == null) {
			if (required) {
				throw new RuntimeException("Missing an required module: " + moduleName);
			}
		}
		else {
			modules.add(module);
		}
	}

	/**
	 * @return module handling the TermGenie service implementations
	 */
	protected TermGenieServiceModule getServiceModule() {
		return new TermGenieServiceModule(applicationProperties);
	}

	/**
	 * @return module handling the authentication
	 */
	protected IOCModule getAuthenticationModule() {
		return new AuthenticationModule(TermGenieServletModule.OPENID_SERVLET_PATH, applicationProperties);
	}

	/**
	 * @return module handling the user permissions
	 */
	protected abstract IOCModule getUserPermissionModule();

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
		return new ReasonerModule(applicationProperties);
	}

	/**
	 * @return module providing the commit operations
	 */
	protected IOCModule getCommitModule() {
		return null;
	}

	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(false, applicationProperties);
	}

	/**
	 * @return null or additional modules required for the start-up
	 */
	protected Collection<IOCModule> getAdditionalModules() {
		return null;
	}

}
