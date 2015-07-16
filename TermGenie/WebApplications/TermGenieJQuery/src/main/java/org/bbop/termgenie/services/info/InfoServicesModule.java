package org.bbop.termgenie.services.info;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class InfoServicesModule extends IOCModule {

	public InfoServicesModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(InfoServices.class, InfoServicesImpl.class);
	}

}
