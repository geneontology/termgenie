package org.bbop.termgenie.user.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;


public class GeneOntologyJsonUserDataModule extends IOCModule {

	private final String gocJsonConfigResource;
	
	/**
	 * @param applicationProperties
	 * @param gocJsonConfigResource
	 */
	public GeneOntologyJsonUserDataModule(Properties applicationProperties,
			String gocJsonConfigResource)
	{
		super(applicationProperties);
		this.gocJsonConfigResource = gocJsonConfigResource;
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GeneOntologyJsonUserDataProvider.class);
		bind(GeneOntologyJsonUserDataProvider.ConfigResourceName, gocJsonConfigResource);
	}

}
