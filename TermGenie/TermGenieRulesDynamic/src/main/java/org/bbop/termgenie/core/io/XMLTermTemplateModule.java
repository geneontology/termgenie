package org.bbop.termgenie.core.io;

import org.bbop.termgenie.core.ioc.IOCModule;

public class XMLTermTemplateModule extends IOCModule {

	@Override
	protected void configure() {
		bind(TermTemplateIO.class).to(XMLTermTemplateIO.class);
	}

}
