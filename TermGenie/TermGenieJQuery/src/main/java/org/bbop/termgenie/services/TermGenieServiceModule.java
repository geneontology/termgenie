package org.bbop.termgenie.services;

import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyLoader;
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
	OntologyTermSuggestor provideOntologyTermSuggestor(OntologyLoader loader) {
		return new LuceneOnlyClient(loader.getOntologies());
	}
	
}
