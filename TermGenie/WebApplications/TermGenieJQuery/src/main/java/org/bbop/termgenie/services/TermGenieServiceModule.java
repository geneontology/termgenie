package org.bbop.termgenie.services;

import java.util.Properties;

import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.solr.LuceneOnlyClient;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module containing the implementations for the TermGenie services.
 */
public class TermGenieServiceModule extends IOCModule {

	/**
	 * @param applicationProperties
	 */
	public TermGenieServiceModule(Properties applicationProperties) {
		super(applicationProperties);
	}
	
	@Override
	protected void configure() {
		bind(GenerateTermsService.class, GenerateTermsServiceImpl.class);
		bind(OntologyService.class, OntologyServiceImpl.class);
		bind(ProgressMonitor.class, ProgressMonitorImpl.class);
		bindSessionHandler();
		bindTermCommitService();
	}

	@Singleton
	@Provides
	protected SessionHandler providesSessionHandler(InternalSessionHandler sessionHandler) {
		return sessionHandler;
	}
	
	protected void bindSessionHandler() {
		bind(InternalSessionHandler.class, InternalSessionHandlerImpl.class);
	}
	
	protected void bindTermCommitService() {
		bind(TermCommitService.class, NoCommitTermCommitServiceImpl.class);
	}

	@Provides
	@Singleton
	OntologyTermSuggestor provideOntologyTermSuggestor(OntologyLoader loader, ReasonerFactory factory)
	{
		return new LuceneOnlyClient(loader.getOntologyManager(), factory);
	}

}
