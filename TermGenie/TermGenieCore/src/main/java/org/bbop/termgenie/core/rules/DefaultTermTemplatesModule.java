package org.bbop.termgenie.core.rules;

import java.io.File;

import org.bbop.termgenie.core.io.TermTemplateIO;
import org.bbop.termgenie.core.io.TermTemplateIOModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class DefaultTermTemplatesModule extends IOCModule {

	@Override
	protected void configure() {
		bind(DefaultTermTemplates.class);
	}

	public static class DefaultTermTemplatesWriter {
		
		public static void main(String[] args) {
			Injector injector = Guice.createInjector(new DefaultOntologyModule(), 
					new DefaultTermTemplatesModule(), new TermTemplateIOModule());
			TermTemplateIO writer = injector.getInstance(TermTemplateIO.class);
			DefaultTermTemplates templates = injector.getInstance(DefaultTermTemplates.class);
			File outputFile = new File("src/main/resources/termgenie_rules.txt");
			writer.writeTemplates(templates.defaultTemplates, outputFile);
		}
	}
}
