package org.bbop.termgenie.user.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;

public class GoYamlUserDataModule extends IOCModule {

	/**
	 * @param applicationProperties
	 */
	public GoYamlUserDataModule(Properties applicationProperties)
	{
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GoYamlUserDataProvider.class);
	}

}
