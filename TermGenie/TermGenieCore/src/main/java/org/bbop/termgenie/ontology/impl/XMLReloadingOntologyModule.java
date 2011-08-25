package org.bbop.termgenie.ontology.impl;

import org.bbop.termgenie.ontology.OntologyConfiguration;

/**
 * Ontology Module, which periodically reloads ontologies from the source and
 * is configured using an XML files.
 */
public class XMLReloadingOntologyModule extends ReloadingOntologyModule {

	private final String configFile;
	
	/**
	 * @param configFile
	 */
	public XMLReloadingOntologyModule(String configFile) {
		super();
		this.configFile = configFile;
	}

	public XMLReloadingOntologyModule() {
		this(XMLOntologyConfiguration.SETTINGS_FILE);
	}

	@Override
	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
		bind("XMLOntologyConfigurationResource", configFile);
	}
}
