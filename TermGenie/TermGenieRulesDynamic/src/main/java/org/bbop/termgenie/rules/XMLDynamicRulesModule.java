package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
	
	private final boolean useIsInferred;
	private final boolean assertInferences;
	private final boolean filterNonAsciiSynonyms;
	private final String ruleFile;
	
	@Override
	protected void configure() {
		bind(TermGenieScriptRunner.USE_IS_INFERRED_BOOLEAN_NAME, useIsInferred);
		bind(TermGenieScriptRunner.ASSERT_INFERERNCES_BOOLEAN_NAME, assertInferences);
		bind(TermGenieScriptRunner.FILTER_NON_ASCII_SYNONYMS, filterNonAsciiSynonyms);
		bind(TermGenerationEngine.class, TermGenieScriptRunner.class);
		bindTemplateIO();
	}
	
	/**
	 * @param ruleFile
	 * @param useIsInferred
	 * @param assertInferences
	 * @param filterNonAsciiSynonyms
	 * @param applicationProperties
	 */
	public XMLDynamicRulesModule(String ruleFile, boolean useIsInferred, boolean assertInferences, boolean filterNonAsciiSynonyms, Properties applicationProperties) {
		super(applicationProperties);
		this.useIsInferred = useIsInferred;
		this.assertInferences = assertInferences;
		this.ruleFile = ruleFile;
		this.filterNonAsciiSynonyms = filterNonAsciiSynonyms;
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
		TermGenieScriptRunner scriptRunner = injector.getInstance(TermGenieScriptRunner.class);
		InputStream stream = getResourceInputStream(templateResource, injector.getInstance(Key.get(Boolean.class, Names.named(GlobalConfigModule.TryResourceLoadAsFilesName))));
		try {
			List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
			String templates = IOUtils.toString(stream);
			result.add(Pair.of(templateResource, templates));
			for(Entry<TermTemplate, String> scriptEntry : scriptRunner.scripts.entrySet()) {
				result.add(Pair.of(scriptEntry.getKey().getName(), scriptEntry.getValue()));
			}
			return result;
		} catch (IOException exception) {
			Logger.getLogger(getClass()).warn("Could not read template resource: "+templateResource, exception);
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
		return null;
	}
}
