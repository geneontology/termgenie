package org.bbop.termgenie.core.rules;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.name.Names;

public class ReasonerModule extends IOCModule {

	public static final String NAMED_DIRECT_REASONER_FACTORY = "DirectReasonerFactory";
	
	/**
	 * @param applicationProperties
	 */
	public ReasonerModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(ReasonerFactory.class, CachingReasonerFactoryImpl.class);
		bind(ReasonerFactory.class).annotatedWith(Names.named(NAMED_DIRECT_REASONER_FACTORY)).to(ReasonerFactoryImpl.class);
	}
	
}
