package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.io.TermTemplateIO;
import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Provides;
import com.google.inject.name.Named;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules
 * from an external xml file.
 */
public class XMLDynamicRulesModule extends IOCModule {

	private final String ruleFile;
	
	@Override
	protected void configure() {
		bind(TermGenerationEngine.class).to(TermGenieScriptRunner.class);
		bindTemplateIO();
	}
	
	/**
	 * @param ruleFile
	 * @param applicationProperties
	 */
	public XMLDynamicRulesModule(String ruleFile, Properties applicationProperties) {
		super(applicationProperties);
		this.ruleFile = ruleFile;
	}
	
	protected void bindTemplateIO() {
		install(new XMLTermTemplateModule(applicationProperties));
		bind("DynamicRulesTemplateResource", ruleFile);
	}

	@Provides
	@Singleton
	List<TermTemplate> providesTermTemplates(TermTemplateIO templateIO,
			@Named("DynamicRulesTemplateResource") String templateResource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles)
	{
		InputStream in = null;
		try {
			in = getResourceInputStream(templateResource, tryResourceLoadAsFiles);
			return templateIO.readTemplates(in);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}
	
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
