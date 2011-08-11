package org.bbop.termgenie.rules;

import java.io.InputStream;

import org.bbop.termgenie.core.io.TermTemplateIOModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.tools.ResourceLoader;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules
 * from an external flat file.
 */
public class DefaultDynamicRulesModule extends DynamicRulesModule {

	private static MyResourceLoader loader = new MyResourceLoader();

	@Override
	protected void configure() {
		super.configure();
		install(new TermTemplateIOModule());
		bind("DynamicRulesTemplateResource", "default_termgenie_rules.txt");
	}

	@Override
	protected InputStream getResourceInputStream(String resource) {
		InputStream in = loader.loadResource(resource);
		return in;
	}

	private static class MyResourceLoader extends ResourceLoader {

		@Override
		public InputStream loadResource(String name) {
			return super.loadResource(name);
		}

	}
}
