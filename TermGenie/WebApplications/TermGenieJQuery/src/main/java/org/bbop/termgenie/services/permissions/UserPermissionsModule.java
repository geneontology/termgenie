package org.bbop.termgenie.services.permissions;

import org.bbop.termgenie.core.ioc.IOCModule;


public class UserPermissionsModule extends IOCModule {

	@Override
	protected void configure() {
		bind(UserPermissions.class).to(JsonFileUserPermissionsImpl.class);
		bind("JsonUserPermissionsFileName");
	}

}
