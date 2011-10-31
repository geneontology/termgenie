package org.bbop.termgenie.ontology.impl;

import java.util.Properties;

import org.bbop.termgenie.ontology.OntologyConfiguration;

/**
 * Ontology Module, which periodically reloads ontologies from the source and
 * is configured using an XML files.
 */
public class XMLReloadingOntologyModule extends ReloadingOntologyModule {

	private final String configFile;
	
	/**
	 * @param configFile
	 * @param applicationProperties 
	 */
	public XMLReloadingOntologyModule(String configFile, Properties applicationProperties) {
		super(applicationProperties);
		this.configFile = configFile;
	}

	public XMLReloadingOntologyModule(Properties applicationProperties) {
		this(XMLOntologyConfiguration.SETTINGS_FILE, applicationProperties);
	}

	@Override
	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
		bind("XMLOntologyConfigurationResource", configFile);
	}
}
