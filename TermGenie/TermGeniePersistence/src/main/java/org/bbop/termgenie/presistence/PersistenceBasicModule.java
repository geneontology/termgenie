package org.bbop.termgenie.presistence;

import java.io.File;

import javax.persistence.EntityManager;

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
	 */
	public PersistenceBasicModule(File dbFolder, String dbType) {
		super();
		this.dbFolder = dbFolder;
		this.dbType = dbType;
	}
	
	/**
	 * @param dbFolder
	 */
	public PersistenceBasicModule(File dbFolder) {
		this(dbFolder, EntityManagerFactoryProvider.HSQLDB);
	}

	@Override
	protected void configure() {
		bind("PersistenceDatabaseFolder", dbFolder);
		bind("PersistenceDatabaseType", dbType);
	}

	@Provides
	@Singleton
	public EntityManager provideEntityManagerFactory(@Named("PersistenceDatabaseFolder") File folder,
			@Named("PersistenceDatabaseType") String type)
	{
		try {
			return provider.createFactory(folder, type, "TermGenie").createEntityManager();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
