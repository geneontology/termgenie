package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.io.TermTemplateIO;
import org.bbop.termgenie.core.io.TermTemplateIOModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

import com.google.inject.Provides;

/**
 * Module which provides a {@link TermGenerationEngine}, using rules extracted from an external source. 
 */
public abstract class DynamicRulesModule extends IOCModule {

	@Override
	protected void configure() {
		install(new TermTemplateIOModule());
		bind(TermGenerationEngine.class).to(TermGenieScriptRunner.class);
	}
	
	@Provides @Singleton
	List<TermTemplate> providesTermTemplates(TermTemplateIO templateIO) {
		InputStream in = null;
		try {
			in = getResourceInputStream();
			return templateIO.readTemplates(in);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected abstract InputStream getResourceInputStream();
	
}
