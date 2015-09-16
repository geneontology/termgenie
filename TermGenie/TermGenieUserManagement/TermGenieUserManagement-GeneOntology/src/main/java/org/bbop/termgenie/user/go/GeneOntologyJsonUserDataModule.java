package org.bbop.termgenie.user.go;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;

@Deprecated
public class GeneOntologyJsonUserDataModule extends IOCModule {

	private final String gocJsonConfigResource;
	private final List<String> additionalXrefResources;
	
	/**
	 * @param applicationProperties
	 * @param gocJsonConfigResource
	 * @param additionalXrefResources 
	 */
	public GeneOntologyJsonUserDataModule(Properties applicationProperties,
			String gocJsonConfigResource,
			List<String> additionalXrefResources)
	{
		super(applicationProperties);
		this.gocJsonConfigResource = gocJsonConfigResource;
		this.additionalXrefResources = additionalXrefResources;
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GeneOntologyJsonUserDataProvider.class);
		bind(GeneOntologyJsonUserDataProvider.ConfigResourceName, gocJsonConfigResource);
		bindList(GeneOntologyJsonUserDataProvider.AdditionalXrefResourcesName, additionalXrefResources);
	}

}
