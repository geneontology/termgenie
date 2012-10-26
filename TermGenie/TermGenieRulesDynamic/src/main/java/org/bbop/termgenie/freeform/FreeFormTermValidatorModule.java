package org.bbop.termgenie.freeform;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class FreeFormTermValidatorModule extends IOCModule {

	public FreeFormTermValidatorModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(FreeFormTermValidator.class, FreeFormTermValidatorImpl.class);
	}

}
