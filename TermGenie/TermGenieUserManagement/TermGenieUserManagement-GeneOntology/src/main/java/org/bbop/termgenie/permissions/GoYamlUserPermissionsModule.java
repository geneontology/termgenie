package org.bbop.termgenie.permissions;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class GoYamlUserPermissionsModule extends IOCModule {

	private final String applicationName;
	
	/**
	 * @param applicationName
	 * @param applicationProperties
	 */
	public GoYamlUserPermissionsModule(String applicationName, Properties applicationProperties) {
		super(applicationProperties);
		this.applicationName = applicationName;
	}

	@Override
	protected void configure() {
		bind(UserPermissions.class, GoYamlUserPermissionsImpl.class);
		bind("YamlUserPermissionsApplicationName", applicationName);
	}
}
