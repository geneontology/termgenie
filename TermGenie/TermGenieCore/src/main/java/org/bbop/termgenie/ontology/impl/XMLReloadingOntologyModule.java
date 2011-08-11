package org.bbop.termgenie.ontology.impl;

import org.bbop.termgenie.ontology.OntologyConfiguration;

/**
 * Ontology Module, which priodically reloads ontologies from the source and
 * reads the configs from xml files.
 */
public class XMLReloadingOntologyModule extends ReloadingOntologyModule {

	@Override
	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
		bind("XMLOntologyConfigurationResource", XMLOntologyConfiguration.SETTINGS_FILE);
	}
}
