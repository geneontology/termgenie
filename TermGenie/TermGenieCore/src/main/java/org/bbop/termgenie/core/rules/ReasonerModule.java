package org.bbop.termgenie.core.rules;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.name.Names;

public class ReasonerModule extends IOCModule {

	public static final String NAMED_DIRECT_REASONER_FACTORY = "DirectReasonerFactory";

	@Override
	protected void configure() {
		bind(ReasonerFactory.class).to(CachingReasonerFactoryImpl.class);
		bind(ReasonerFactoryImpl.REASONER_FACTORY_DEFAULT_REASONER, ReasonerFactoryImpl.JCEL);
		bind(ReasonerFactory.class).annotatedWith(Names.named(NAMED_DIRECT_REASONER_FACTORY)).to(ReasonerFactoryImpl.class);
	}
	
	
}
