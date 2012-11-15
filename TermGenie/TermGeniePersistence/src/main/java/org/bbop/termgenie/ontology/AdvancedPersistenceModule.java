package org.bbop.termgenie.ontology;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Configure all the persistence using tools.
 * 
 * Requires {@link PersistenceBasicModule} or similar for the {@link EntityManager}
 * 
 * Provides two {@link OntologyIdManager}: Primary and Secondary
 */
public class AdvancedPersistenceModule extends IOCModule {

	private final String primaryOntologyIdManagerName;
	private final String primaryOntologyIdStoreConfigurationFile;
	
	private final String secondaryOntologyIdManagerName;
	private final String secondaryOntologyIdStoreConfigurationFile;
	
	public AdvancedPersistenceModule(String primaryOntologyIdManagerName,
			String primaryOntologyIdStoreConfigurationFile,
			String secondaryOntologyIdManagerName,
			String secondaryOntologyIdStoreConfigurationFile,
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.primaryOntologyIdManagerName = primaryOntologyIdManagerName;
		this.primaryOntologyIdStoreConfigurationFile = primaryOntologyIdStoreConfigurationFile;
		this.secondaryOntologyIdManagerName = secondaryOntologyIdManagerName;
		this.secondaryOntologyIdStoreConfigurationFile = secondaryOntologyIdStoreConfigurationFile;
	}

	@Override
	protected void configure() {
		bindCommitHistoryStore();
	}

	@Provides
	@Singleton
	@Named("PrimaryOntologyIdManager")
	protected OntologyIdManager providePrimaryOntologyIdManager(@Named("IdEntityManagerFactory") EntityManagerFactory entityManagerFactory,
			@Named("TryResourceLoadAsFiles") boolean tryLoadAsFiles)
	{
		OntologyIdStoreConfiguration configuration = new PlainOntologyIdStoreConfiguration(primaryOntologyIdStoreConfigurationFile, tryLoadAsFiles);
		OntologyIdProvider idProvider =  new OntologyIdProviderImpl(configuration, entityManagerFactory);
		return new OntologyIdManager(primaryOntologyIdManagerName, idProvider);
	}
	
	@Provides
	@Singleton
	@Named("SecondaryOntologyIdManager")
	protected OntologyIdManager provideSecondaryOntologyIdManager(@Named("SecondaryIdEntityManagerFactory") EntityManagerFactory entityManagerFactory,
			@Named("TryResourceLoadAsFiles") boolean tryLoadAsFiles)
	{
		OntologyIdStoreConfiguration configuration = new PlainOntologyIdStoreConfiguration(secondaryOntologyIdStoreConfigurationFile, tryLoadAsFiles);
		OntologyIdProvider idProvider =  new OntologyIdProviderImpl(configuration, entityManagerFactory);
		return new OntologyIdManager(secondaryOntologyIdManagerName, idProvider);
	}
	
	protected void bindCommitHistoryStore() {
		bind(CommitHistoryStore.class, CommitHistoryStoreImpl.class);
	}
}
