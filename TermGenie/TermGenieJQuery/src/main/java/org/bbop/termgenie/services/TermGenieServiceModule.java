package org.bbop.termgenie.services;

import java.util.Collection;
import java.util.List;

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

public class TermGenieServiceModule extends IOCModule {

	@Override
	protected void configure() {
		bind(GenerateTermsService.class).to(GenerateTermsServiceImpl.class);
		bind(OntologyService.class).to(OntologyServiceImpl.class);
		bind(SessionHandler.class).to(SessionHandlerImpl.class);
		bind(TermCommitService.class).to(TermCommitServiceImpl.class);
	}
	
	@Provides @Singleton
	OntologyTermSuggestor provideOntologyTermSuggestor(OntologyConfiguration configuration, OntologyLoader loader, ReasonerFactory factory) {
		List<OntologyTaskManager> managers = loader.getOntologies();
		Collection<ConfiguredOntology> ontologies = configuration.getOntologyConfigurations().values();
		return new LuceneOnlyClient(ontologies, managers, factory);
	}
	
}
