package org.bbop.termgenie.ontology.impl;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;

/**
 * Abstract module providing the default ontologies.
 * 
 * @see OntologyLoader
 * @see OntologyConfiguration
 * @see IRIMapper
 */
public abstract class DefaultOntologyModule extends IOCModule {

	private final String ontologyConfigurationFile;

	public DefaultOntologyModule(Properties applicationProperties, String ontologyConfigurationFile) {
		super(applicationProperties);
		this.ontologyConfigurationFile = ontologyConfigurationFile;
	}

	@Override
	protected void configure() {
		bindOntologyLoader();

		bindOntologyConfiguration();

		bindIRIMapper();
	}

	protected void bindOntologyLoader() {
		bind(OntologyLoader.class, DefaultOntologyLoader.class);
	}

	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
		bind("XMLOntologyConfigurationResource", ontologyConfigurationFile);
	}

	protected abstract void bindIRIMapper();

}
