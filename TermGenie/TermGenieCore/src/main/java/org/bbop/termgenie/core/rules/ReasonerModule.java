package org.bbop.termgenie.core.rules;

import org.bbop.termgenie.core.ioc.IOCModule;

public class ReasonerModule extends IOCModule {

	@Override
	protected void configure() {
		bind(ReasonerFactory.class).to(ReasonerFactoryImpl.class);
		bind("ReasonerFactoryDefaultReasoner", ReasonerFactoryImpl.JCEL);
	}
}
