package org.bbop.termgenie.ontology;

import javax.persistence.EntityManager;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;

/**
 * Configure all the persistence using tools.
 * 
 * Requires {@link PersistenceBasicModule} or similar for the {@link EntityManager}
 */
public class AdvancedPersistenceModule extends IOCModule {

	private final String ontologyIdManagerName;
	private final String ontologyIdStoreConfigurationFile;
	
	/**
	 * @param ontologyIdManagerName
	 * @param ontologyIdStoreConfigurationFile
	 */
	public AdvancedPersistenceModule(String ontologyIdManagerName,
			String ontologyIdStoreConfigurationFile)
	{
		super();
		this.ontologyIdManagerName = ontologyIdManagerName;
		this.ontologyIdStoreConfigurationFile = ontologyIdStoreConfigurationFile;
	}

	@Override
	protected void configure() {
		bindCommitHistoryStore();
		bindOntologyIdProvider();
		bind(OntologyIdManager.class);
		bind("OntologyIdManagerName", ontologyIdManagerName);
		bindOntologyIdStoreConfiguration();
	}

	protected void bindOntologyIdStoreConfiguration() {
		bind(OntologyIdStoreConfiguration.class).to(PlainOntologyIdStoreConfiguration.class);
		bind("PlainOntologyIdStoreConfigurationResource", ontologyIdStoreConfigurationFile);
	}

	protected void bindOntologyIdProvider() {
		bind(OntologyIdProvider.class).to(OntologyIdProviderImpl.class);
	}

	protected void bindCommitHistoryStore() {
		bind(CommitHistoryStore.class).to(CommitHistoryStoreImpl.class);
	}
}
