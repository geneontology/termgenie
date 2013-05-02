package org.bbop.termgenie.services.freeform;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.freeform.FreeFormTermValidatorModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class FreeFormTermServiceModule extends FreeFormTermValidatorModule {
	
	private final String defaultOntology;

	public FreeFormTermServiceModule(Properties applicationProperties,
			boolean addSubsetTag,
			String defaultOntology,
			List<String> oboNamespaces,
			String subset)
	{
		super(applicationProperties, addSubsetTag, oboNamespaces, subset);
		this.defaultOntology = defaultOntology;
	}

	@Override
	protected void configure() {
		super.configure();
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}
	
	@Singleton
	@Provides
	@Named("FreeFormDefaultOntology")
	protected Ontology provideFreeFormDefaultOntology(OntologyConfiguration configuration) {
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get(defaultOntology);
		return configuredOntology;
	}

}
