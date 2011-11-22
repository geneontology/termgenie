package org.bbop.termgenie.services.management;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class ManagementServiceModule extends IOCModule {

	public ManagementServiceModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(ManagementServices.class, ManagementServicesImpl.class);
	}

}
