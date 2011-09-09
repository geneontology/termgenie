package org.bbop.termgenie.services;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.solr.LuceneOnlyClient;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Module containing the implementations for the TermGenie services.
 */
public class TermGenieServiceModule extends IOCModule {

	private final long defaultSessionHandlerImplTimeout;
	private final TimeUnit defaultSessionHandlerImplTimeUnit;
	
	/**
	 * @param defaultSessionHandlerImplTimeout
	 * @param defaultSessionHandlerImplTimeUnit
	 */
	public TermGenieServiceModule(long defaultSessionHandlerImplTimeout,
			TimeUnit defaultSessionHandlerImplTimeUnit)
	{
		super();
		this.defaultSessionHandlerImplTimeout = defaultSessionHandlerImplTimeout;
		this.defaultSessionHandlerImplTimeUnit = defaultSessionHandlerImplTimeUnit;
	}
	
	
	public TermGenieServiceModule() {
		this(90L, TimeUnit.MINUTES);
	}

	@Override
	protected void configure() {
		bind(GenerateTermsService.class).to(GenerateTermsServiceImpl.class);
		bind(OntologyService.class).to(OntologyServiceImpl.class);
		bindSessionHandler();
		bindTermCommitService();
	}

	protected void bindSessionHandler() {
		bind(SessionHandler.class).to(SessionHandlerImpl.class);
		bindSessionHandlerParameters();
	}
	
	protected void bindSessionHandlerParameters() {
		bind("SessionHandlerImplTimeout", defaultSessionHandlerImplTimeout);
		bind("SessionHandlerImplTimeUnit", defaultSessionHandlerImplTimeUnit);
	}

	protected void bindTermCommitService() {
		bind(TermCommitService.class).to(NoCommitTermCommitServiceImpl.class);
	}

	@Provides
	@Singleton
	OntologyTermSuggestor provideOntologyTermSuggestor(OntologyConfiguration configuration,
			OntologyLoader loader,
			ReasonerFactory factory)
	{
		List<OntologyTaskManager> managers = loader.getOntologies();
		Collection<ConfiguredOntology> ontologies = configuration.getOntologyConfigurations().values();
		return new LuceneOnlyClient(ontologies, managers, factory);
	}

}
