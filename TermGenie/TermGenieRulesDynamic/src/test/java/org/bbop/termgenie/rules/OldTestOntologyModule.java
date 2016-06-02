package org.bbop.termgenie.rules;

import java.util.List;

import org.bbop.termgenie.ontology.impl.OntologyModule;

public class OldTestOntologyModule extends OntologyModule {

	public OldTestOntologyModule() {
		this("ontology-configuration_simple.xml");
	}
	
	public OldTestOntologyModule(String config) {
		super(config);
	}
	
	public OldTestOntologyModule(String config, List<String> ignores) {
		super(config);
		setFileCacheIgnoreMappings(ignores);
	}
}