package org.bbop.termgenie.core.io;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

/**
 * Module providing XML based {@link TermTemplateIO}.
 */
public class XMLTermTemplateModule extends IOCModule {

	public XMLTermTemplateModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(TermTemplateIO.class, XMLTermTemplateIO.class);
	}

}
