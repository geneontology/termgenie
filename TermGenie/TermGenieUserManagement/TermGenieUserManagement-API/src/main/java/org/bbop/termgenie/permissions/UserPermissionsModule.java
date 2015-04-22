package org.bbop.termgenie.permissions;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class UserPermissionsModule extends IOCModule {

	private final String applicationName;
	
	/**
	 * @param applicationName
	 * @param applicationProperties
	 */
	public UserPermissionsModule(String applicationName, Properties applicationProperties) {
		super(applicationProperties);
		this.applicationName = applicationName;
	}

	@Override
	protected void configure() {
		bind(UserPermissions.class, JsonFileUserPermissionsImpl.class);
		bind("JsonUserPermissionsFileName");
		bind("JsonUserPermissionsApplicationName", applicationName);
	}
}
