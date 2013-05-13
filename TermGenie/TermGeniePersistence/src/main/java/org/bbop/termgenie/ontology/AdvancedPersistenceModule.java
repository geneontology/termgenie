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

	private final String defaultPrimaryOntologyIdManagerName;
	private final String defaultPrimaryOntologyIdStoreConfigurationFile;
	
	private final String defaultSecondaryOntologyIdManagerName;
	private final String defaultSecondaryOntologyIdStoreConfigurationFile;
	
	public AdvancedPersistenceModule(String primaryOntologyIdManagerName,
			String primaryOntologyIdStoreConfigurationFile,
			String secondaryOntologyIdManagerName,
			String secondaryOntologyIdStoreConfigurationFile,
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.defaultPrimaryOntologyIdManagerName = primaryOntologyIdManagerName;
		this.defaultPrimaryOntologyIdStoreConfigurationFile = primaryOntologyIdStoreConfigurationFile;
		this.defaultSecondaryOntologyIdManagerName = secondaryOntologyIdManagerName;
		this.defaultSecondaryOntologyIdStoreConfigurationFile = secondaryOntologyIdStoreConfigurationFile;
	}

	@Override
	protected void configure() {
		bindCommitHistoryStore();
		bind("PrimaryOntologyIdManagerName", defaultPrimaryOntologyIdManagerName);
		bind("PrimaryOntologyIdStoreConfigurationFile", defaultPrimaryOntologyIdStoreConfigurationFile);
		bind("secondaryOntologyIdManagerName", defaultSecondaryOntologyIdManagerName);
		bind("SecondaryOntologyIdStoreConfigurationFile", defaultSecondaryOntologyIdStoreConfigurationFile);
	}

	@Provides
	@Singleton
	@Named("PrimaryOntologyIdManager")
	protected OntologyIdManager providePrimaryOntologyIdManager(@Named("IdEntityManagerFactory") EntityManagerFactory entityManagerFactory,
			String primaryOntologyIdManagerName,
			@Named("PrimaryOntologyIdStoreConfigurationFile") String primaryOntologyIdStoreConfigurationFile,
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
			String secondaryOntologyIdManagerName,
			@Named("SecondaryOntologyIdStoreConfigurationFile") String secondaryOntologyIdStoreConfigurationFile,
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
