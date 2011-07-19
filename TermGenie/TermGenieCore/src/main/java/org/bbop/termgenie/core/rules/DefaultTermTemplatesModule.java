package org.bbop.termgenie.core.rules;

import org.bbop.termgenie.core.ioc.IOCModule;

public class DefaultTermTemplatesModule extends IOCModule {

	@Override
	protected void configure() {
		bind(DefaultTermTemplates.class);
	}

}
