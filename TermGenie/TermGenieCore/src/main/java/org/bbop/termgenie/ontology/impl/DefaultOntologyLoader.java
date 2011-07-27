package org.bbop.termgenie.ontology.impl;

import java.util.Set;

import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Default ontology loader, which does NOT reload ontology files periodically.
 */
@Singleton
public class DefaultOntologyLoader extends ReloadingOntologyLoader {
	
	@Inject
	DefaultOntologyLoader(OntologyConfiguration configuration, 
			IRIMapper iriMapper, 
			OntologyCleaner cleaner, 
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies)
	{
		super(configuration, iriMapper, cleaner, skipOntologies, 0, null);
	}

}