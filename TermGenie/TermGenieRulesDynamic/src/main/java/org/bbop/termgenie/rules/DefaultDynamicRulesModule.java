package org.bbop.termgenie.rules;

import java.io.InputStream;

import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.tools.ResourceLoader;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules from an external file. 
 */
public class DefaultDynamicRulesModule extends DynamicRulesModule {

	private static MyResourceLoader loader = new MyResourceLoader();

	@Override
	protected InputStream getResourceInputStream() {
		InputStream in = loader.loadResource("default_termgenie_rules.txt");
		return in;
	}
	
	private static class MyResourceLoader extends ResourceLoader {

		@Override
		public InputStream loadResource(String name) {
			return super.loadResource(name);
		}
		
	}
}
