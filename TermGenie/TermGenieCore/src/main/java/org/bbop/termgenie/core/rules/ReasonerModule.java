package org.bbop.termgenie.core.rules;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class ReasonerModule extends IOCModule {

	/**
	 * @param applicationProperties
	 */
	public ReasonerModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(ReasonerFactory.class, ReasonerFactoryImpl.class);
	}
	
}
