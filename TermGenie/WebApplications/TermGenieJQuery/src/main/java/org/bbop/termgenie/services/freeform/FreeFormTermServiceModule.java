package org.bbop.termgenie.services.freeform;

import java.util.Properties;

import org.bbop.termgenie.freeform.FreeFormTermValidatorModule;


public class FreeFormTermServiceModule extends FreeFormTermValidatorModule {

	public FreeFormTermServiceModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}

}
