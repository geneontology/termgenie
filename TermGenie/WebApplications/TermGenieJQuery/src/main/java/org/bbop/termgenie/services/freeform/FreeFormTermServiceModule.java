package org.bbop.termgenie.services.freeform;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class FreeFormTermServiceModule extends IOCModule {

	public FreeFormTermServiceModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}

}
