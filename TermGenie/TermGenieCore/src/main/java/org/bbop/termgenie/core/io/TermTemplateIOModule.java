package org.bbop.termgenie.core.io;

import org.bbop.termgenie.core.ioc.IOCModule;

/**
 * Module providing IO for term templates.
 * 
 * @see TermTemplateIO
 */
public class TermTemplateIOModule extends IOCModule {

	@Override
	protected void configure() {
		bind(TermTemplateIO.class).to(FlatFileTermTemplateIO.class);
		bind(TemplateOntologyHelper.class).to(TemplateOntologyHelperImpl.class);
	}

}
