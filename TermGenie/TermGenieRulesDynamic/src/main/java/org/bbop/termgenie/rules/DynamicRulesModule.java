package org.bbop.termgenie.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Singleton;

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
		InputStream in = getResourceInputStream();
		BufferedReader reader  = new BufferedReader(new InputStreamReader(in));
		try {
			return templateIO.readTemplates(reader);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected abstract InputStream getResourceInputStream();
	
}
