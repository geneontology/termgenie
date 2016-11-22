package org.bbop.termgenie.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.ProgressMonitor;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.authenticate.AuthenticationModule;
import org.bbop.termgenie.services.authenticate.BrowserIdHandler;
import org.bbop.termgenie.services.freeform.FreeFormTermService;
import org.bbop.termgenie.services.freeform.NoopFreeFormModule;
import org.bbop.termgenie.services.history.RecentSubmissionsService;
import org.bbop.termgenie.services.history.RecentSubmissionsServiceModule;
import org.bbop.termgenie.services.info.InfoServices;
import org.bbop.termgenie.services.info.InfoServicesModule;
import org.bbop.termgenie.services.lookup.TermLookupServiceDefaultModule;
import org.bbop.termgenie.services.management.ManagementServiceModule;
import org.bbop.termgenie.services.management.ManagementServices;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.bbop.termgenie.services.visualization.TermHierarchyModule;
import org.bbop.termgenie.services.visualization.TermHierarchyRenderer;
import org.bbop.termgenie.user.simple.SimpleUserDataModule;
import org.json.rpc.server.InjectingJsonRpcExecutor;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public abstract class AbstractTermGenieContextListener extends GuiceServletContextListener implements HttpSessionListener {

	private static long sessionsCreated = 0;
	private static long sessionsDestroyed = 0;
	private static int activeSessions = 0;
	
	protected final class TermGenieServletModule extends ServletModule {

		@Override
		protected void configureServlets() {
			serve("/jsonrpc").with(TermGenieJsonRPCServlet.class);
			serve("/termlookup").with(TermLookupServlet.class);
			serve("/gh-request").with(GHAuthenticationRequestServlet.class);
			serve("/gh-access").with(GHAuthenticationAccessServlet.class);
		}

		@Provides
		@Singleton
		protected InjectingJsonRpcExecutor providesInjectingJsonRpcExecutor(GenerateTermsService generate,
				OntologyService ontology,
				TermCommitService commit,
				SessionHandler user,
				BrowserIdHandler browserId,
				TermCommitReviewService review,
				RecentSubmissionsService history,
				ManagementServices management,
				TermHierarchyRenderer renderer,
				FreeFormTermService freeform,
				InfoServices info,
				ProgressMonitor progress)
		{
			InjectingJsonRpcExecutor executor = new InjectingJsonRpcExecutor(getInjector());
			executor.addHandler("progress", progress, ProgressMonitor.class);
			executor.addHandler("generate", generate, GenerateTermsService.class);
			executor.addHandler("ontology", ontology, OntologyService.class);
			executor.addHandler("commit", commit, TermCommitService.class);
			executor.addHandler("user", user, SessionHandler.class);
			executor.addHandler("browserid", browserId, BrowserIdHandler.class);
			executor.addHandler("review", review, TermCommitReviewService.class);
			executor.addHandler("recent", history, RecentSubmissionsService.class);
			executor.addHandler("management", management, ManagementServices.class);
			executor.addHandler("renderer", renderer, TermHierarchyRenderer.class);
			executor.addHandler("freeform", freeform, FreeFormTermService.class);
			executor.addHandler("info", info, InfoServices.class);
			return executor;
		}
	}

	protected final Properties applicationProperties;
	private Injector injector = null;
	
	
	protected AbstractTermGenieContextListener(String applicationPropertyConfigName) {
		applicationProperties = IOCModule.getGlobalSystemProperties(applicationPropertyConfigName);
	}
	
	protected AbstractTermGenieContextListener(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Override
	protected final synchronized Injector getInjector() {
		if (injector == null) {
			ServletModule module = new TermGenieServletModule();
			injector = TermGenieGuice.createWebInjector(module,
					applicationProperties,
					getConfiguration());
		}
		return injector;
	}

	private IOCModule[] getConfiguration() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		modules.add(new ManagementServiceModule(applicationProperties));
		add(modules, getServiceModule(), true, "ServiceModule");
		add(modules, getAuthenticationModule(), true, "Authentication");
		add(modules, getUserPermissionModule(), true, "UserPermission");
		add(modules, getOntologyModule(), true, "OntologyModule");
		add(modules, getReasoningModule(), true, "ReasoningModule");
		add(modules, getRulesModule(), true, "RulesModule");
		add(modules, getUserDataModule(), true, "UserDataModule");
		add(modules, getCommitModule(), false, "CommitModule");
		add(modules, getCommitReviewWebModule(), true, "CommitReviewModule");
		add(modules, getRecentSubmissionsWebModule(), true, "RecentSubmissionsModule");
		add(modules, getTermHierarchyModule(), true, "TermHierarchyModule");
		add(modules, getReviewMailHandlerModule(), true, "ReviewMailHandlerModule");
		add(modules, getTermLookupModule(), true, "TermLookupModule");
		add(modules, getFreeFormTermModule(), true, "FreeFormModule");
		add(modules, getInfoServicesModule(), true, "InfoServiceModule");
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
		return new AuthenticationModule(applicationProperties);
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
	 * @return module providing the implementations for user data 
	 */
	protected IOCModule getUserDataModule() {
		return new SimpleUserDataModule(applicationProperties);
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
	
	protected RecentSubmissionsServiceModule getRecentSubmissionsWebModule() {
		return new RecentSubmissionsServiceModule(true, applicationProperties);
	}
	
	protected IOCModule getTermHierarchyModule() {
		return new TermHierarchyModule(applicationProperties);
	}
	
	protected IOCModule getTermLookupModule() {
		return new TermLookupServiceDefaultModule(applicationProperties);
	}
	
	/**
	 * @return module providing e-mail functionality
	 */
	protected IOCModule getReviewMailHandlerModule() {
		return new NoopReviewMailHandler.NoopReviewMailHandlerModule(applicationProperties);
	}
	
	protected IOCModule getFreeFormTermModule() {
		return new NoopFreeFormModule(applicationProperties);
	}
	
	protected IOCModule getInfoServicesModule() {
		return new InfoServicesModule(applicationProperties);
	}
	
	/**
	 * @return null or additional modules required for the start-up
	 */
	protected Collection<IOCModule> getAdditionalModules() {
		return null;
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		synchronized (AbstractTermGenieContextListener.class) {
			activeSessions += 1;
			sessionsCreated += 1;
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		synchronized (AbstractTermGenieContextListener.class) {
			if (activeSessions > 0) {
				activeSessions -= 1;
				sessionsDestroyed += 1;
			}
		}
	}
	
	public static int getActiveSessionCount() {
		return activeSessions;
	}

	public static long getSessionsCreated() {
		return sessionsCreated;
	}
	
	public static long getSessionsDestroyed() {
		return sessionsDestroyed;
	}
}
