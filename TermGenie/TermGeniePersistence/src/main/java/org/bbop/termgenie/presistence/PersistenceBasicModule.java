package org.bbop.termgenie.presistence;

import java.io.File;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class PersistenceBasicModule extends IOCModule {

	private final EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();
	private final File dbFolder;
	private final String dbType;

	/**
	 * @param dbFolder
	 * @param dbType
	 * @param applicationProperties 
	 */
	public PersistenceBasicModule(File dbFolder, String dbType, Properties applicationProperties) {
		super(applicationProperties);
		this.dbFolder = dbFolder;
		this.dbType = dbType;
	}

	/**
	 * @param dbFolder
	 * @param applicationProperties 
	 */
	public PersistenceBasicModule(File dbFolder, Properties applicationProperties) {
		this(dbFolder, EntityManagerFactoryProvider.HSQLDB, applicationProperties);
	}

	@Override
	protected void configure() {
		bind("PersistenceDatabaseFolder", dbFolder);
		bind("PersistenceDatabaseType", dbType);
	}

	@Provides
	@Singleton
	@Named("DefaultEntityManagerFactory")
	public EntityManagerFactory provideDefaultEntityManager(@Named("PersistenceDatabaseFolder") File folder,
			@Named("PersistenceDatabaseType") String type)
	{
		try {
			return provider.createFactory(folder,  type, EntityManagerFactoryProvider.MODE_DEFAULT, "TermGenie");
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Provides
	@Singleton
	@Named("IdEntityManagerFactory")
	public EntityManagerFactory provideIdEntityManager(@Named("PersistenceDatabaseFolder") File folder,
			@Named("PersistenceDatabaseType") String type)
	{
		try {
			return provider.createFactory(folder, type, EntityManagerFactoryProvider.MODE_IDS, "TermGenie");
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
