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
	
	private final List<String> xrefResources;
	private final String defaultOntology;

	public FreeFormTermServiceModule(Properties applicationProperties,
			boolean addSubsetTag,
			String defaultOntology,
			List<String> oboNamespaces,
			List<String> xrefResources,
			String subset)
	{
		super(applicationProperties, addSubsetTag, oboNamespaces, subset);
		this.defaultOntology = defaultOntology;
		this.xrefResources = xrefResources;
	}

	@Override
	protected void configure() {
		super.configure();
		bindList("FreeFormXrefResources", xrefResources, true);
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
