package org.bbop.termgenie.user.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;

public class GoYamlUserDataModule extends IOCModule {

	private final String yamlUserDataFileName;
	
	/**
	 * @param applicationProperties
	 * @param yamlUserDataFileName
	 */
	public GoYamlUserDataModule(Properties applicationProperties,
			String yamlUserDataFileName)
	{
		super(applicationProperties);
		this.yamlUserDataFileName = yamlUserDataFileName;
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GoYamlUserDataProvider.class);
		bind("YamlUserDataFileName", yamlUserDataFileName);
	}

}
