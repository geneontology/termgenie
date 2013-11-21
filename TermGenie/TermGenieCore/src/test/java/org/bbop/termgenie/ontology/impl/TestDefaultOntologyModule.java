package org.bbop.termgenie.ontology.impl;

import org.bbop.termgenie.ontology.IRIMapper;

public class TestDefaultOntologyModule extends DefaultOntologyModule {

	public TestDefaultOntologyModule() {
		this("ontology-configuration_simple_go.xml");
	}
	
	public TestDefaultOntologyModule(String filename) {
		super(null, filename);
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, LocalFileIRIMapper.class);
		bind("LocalFileIRIMapperResource", LocalFileIRIMapper.SETTINGS_FILE);
	}
}