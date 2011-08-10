package org.bbop.termgenie.core.rules;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Singleton;

public class ReasonerModule extends IOCModule {

	@Override
	protected void configure() {
		/* intentionally empty */
	}
	
	@Singleton
	public ReasonerFactory provideReasonerFactory() {
		return new ReasonerFactory();
	}

}
