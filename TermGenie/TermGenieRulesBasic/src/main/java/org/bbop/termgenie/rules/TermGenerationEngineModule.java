package org.bbop.termgenie.rules;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

@Deprecated
public class TermGenerationEngineModule extends IOCModule {

	@Override
	protected void configure() {
		bind(TermGenerationEngine.class).to(HardCodedTermGenerationEngine.class);
		bind(DefaultTermTemplates.class);
	}

}
