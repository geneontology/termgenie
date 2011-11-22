package org.bbop.termgenie.user.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;


public class GeneOntologyUserDataModule extends IOCModule {

	private final String gocConfigResource;
	private final char gocConfigResourceSeparator;
	private final String gocMappingResource;
	private final char gocMappingResourceSeparator;
	
	/**
	 * @param applicationProperties
	 * @param gocConfigResource
	 * @param gocConfigResourceSeparator
	 * @param gocMappingResource
	 * @param gocMappingResourceSeparator
	 */
	public GeneOntologyUserDataModule(Properties applicationProperties,
			String gocConfigResource,
			char gocConfigResourceSeparator,
			String gocMappingResource,
			char gocMappingResourceSeparator)
	{
		super(applicationProperties);
		this.gocConfigResource = gocConfigResource;
		this.gocConfigResourceSeparator = gocConfigResourceSeparator;
		this.gocMappingResource = gocMappingResource;
		this.gocMappingResourceSeparator = gocMappingResourceSeparator;
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GeneOntologyUserDataProvider.class);
		bind(GeneOntologyUserDataProvider.ConfigResourceName, gocConfigResource);
		bind(GeneOntologyUserDataProvider.ConfigResourceSeparatorName, gocConfigResourceSeparator);
		bind(GeneOntologyUserDataProvider.MappingResourceName, gocMappingResource);
		bind(GeneOntologyUserDataProvider.MappingResourceSeparatorName, gocMappingResourceSeparator);
	}

}
