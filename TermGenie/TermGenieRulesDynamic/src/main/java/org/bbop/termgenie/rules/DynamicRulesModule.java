package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.io.TermTemplateIO;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

import com.google.inject.Provides;
import com.google.inject.name.Named;

/**
 * Module which provides a {@link TermGenerationEngine}, using rules extracted from an external source. 
 */
public abstract class DynamicRulesModule extends IOCModule {

	@Override
	protected void configure() {
		bind(TermGenerationEngine.class).to(TermGenieScriptRunner.class);
	}
	
	@Provides @Singleton
	List<TermTemplate> providesTermTemplates(TermTemplateIO templateIO, 
			@Named("DynamicRulesTemplateResource") String templateResource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles) {
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

	protected abstract InputStream getResourceInputStream(String templateResource, boolean tryResourceLoadAsFiles);
	
}
