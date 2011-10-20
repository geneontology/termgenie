package org.bbop.termgenie.services.permissions;

import org.bbop.termgenie.core.ioc.IOCModule;


public class UserPermissionsModule extends IOCModule {

	private final String applicationName;
	
	/**
	 * @param applicationName
	 */
	public UserPermissionsModule(String applicationName) {
		super();
		this.applicationName = applicationName;
	}

	@Override
	protected void configure() {
		bind(UserPermissions.class).to(JsonFileUserPermissionsImpl.class);
		bind("JsonUserPermissionsFileName");
		bind("JsonUserPermissionsApplicationName", applicationName);
	}
}
