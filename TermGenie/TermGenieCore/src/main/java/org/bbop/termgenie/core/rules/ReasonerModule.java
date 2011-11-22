package org.bbop.termgenie.core.rules;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.name.Names;

public class ReasonerModule extends IOCModule {

	public static final String NAMED_DIRECT_REASONER_FACTORY = "DirectReasonerFactory";
	private final String defaultReasonerName;
	
	/**
	 * @param defaultReasonerName
	 * @param applicationProperties
	 */
	public ReasonerModule(String defaultReasonerName, Properties applicationProperties) {
		super(applicationProperties);
		this.defaultReasonerName = defaultReasonerName;
	}

	public ReasonerModule(Properties applicationProperties) {
		this(ReasonerFactoryImpl.ELK, applicationProperties);
	}

	@Override
	protected void configure() {
		bind(ReasonerFactory.class, CachingReasonerFactoryImpl.class);
		bind(ReasonerFactoryImpl.REASONER_FACTORY_DEFAULT_REASONER, defaultReasonerName);
		bind(ReasonerFactory.class).annotatedWith(Names.named(NAMED_DIRECT_REASONER_FACTORY)).to(ReasonerFactoryImpl.class);
	}
	
}
