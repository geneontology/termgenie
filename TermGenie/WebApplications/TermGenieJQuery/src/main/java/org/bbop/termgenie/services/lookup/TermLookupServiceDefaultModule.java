package org.bbop.termgenie.services.lookup;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class TermLookupServiceDefaultModule extends IOCModule {

	public TermLookupServiceDefaultModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(TermLookupService.class, TermLookupServiceDefaultImpl.class);
	}

}
