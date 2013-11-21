package org.bbop.termgenie.ontology.impl;

import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Default ontology loader, which does NOT reload ontology files periodically.
 */
@Singleton
public class DefaultOntologyLoader extends ReloadingOntologyLoader {

	@Inject
	DefaultOntologyLoader(OntologyConfiguration configuration, IRIMapper iriMapper) {
		super(configuration, iriMapper, 0, null);
	}

}
