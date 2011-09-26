package org.bbop.termgenie.rules;

import java.io.InputStream;

import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.tools.ResourceLoader;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules
 * from an external xml file.
 */
public class DefaultXMLDynamicRulesModule extends DynamicRulesModule {

	private final String ruleFile;
	
	@Override
	protected void configure() {
		super.configure();
		bindTemplateIO();
	}
	
	/**
	 * @param ruleFile
	 */
	public DefaultXMLDynamicRulesModule(String ruleFile) {
		super();
		this.ruleFile = ruleFile;
	}
	
	public DefaultXMLDynamicRulesModule() {
		this("default_termgenie_rules.xml");
	}

	protected void bindTemplateIO() {
		install(new XMLTermTemplateModule());
		bind("DynamicRulesTemplateResource", ruleFile);
	}

	@Override
	protected InputStream getResourceInputStream(String resource, boolean tryResourceLoadAsFiles) {
		MyResourceLoader loader = new MyResourceLoader(tryResourceLoadAsFiles);
		InputStream in = loader.loadResource(resource);
		return in;
	}

	private static class MyResourceLoader extends ResourceLoader {

		public MyResourceLoader(boolean tryResourceLoadAsFiles) {
			super(tryResourceLoadAsFiles);
		}

		@Override
		public InputStream loadResource(String name) {
			return super.loadResource(name);
		}

	}
	
}
