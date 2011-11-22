package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.io.TermTemplateIO;
import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.ioc.GlobalConfigModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules
 * from an external xml file.
 */
public class XMLDynamicRulesModule extends IOCModule {

	private static final String DynamicRulesTemplateResourceName = "DynamicRulesTemplateResource";
	
	private final String ruleFile;
	
	@Override
	protected void configure() {
		bind(TermGenerationEngine.class, TermGenieScriptRunner.class);
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
		bind(DynamicRulesTemplateResourceName, ruleFile);
	}

	@Provides
	@Singleton
	List<TermTemplate> providesTermTemplates(TermTemplateIO templateIO,
			@Named(DynamicRulesTemplateResourceName) String templateResource,
			@Named(GlobalConfigModule.TryResourceLoadAsFilesName) boolean tryResourceLoadAsFiles)
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

	@Override
	public List<Pair<String, String>> getAdditionalData(Injector injector) {
		String templateResource = injector.getInstance(Key.get(String.class, Names.named(DynamicRulesTemplateResourceName)));
		InputStream stream = getResourceInputStream(templateResource, injector.getInstance(Key.get(Boolean.class, Names.named(GlobalConfigModule.TryResourceLoadAsFilesName))));
		try {
			String templates = IOUtils.toString(stream);
			return Collections.singletonList(new Pair<String, String>(templateResource, templates));
		} catch (IOException exception) {
			Logger.getLogger(getClass()).warn("Could not read template resource: "+templateResource, exception);
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
		return null;
	}
}
