package org.bbop.termgenie.rules;

import java.io.IOException;
import java.util.List;

import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import owltools.io.CatalogXmlIRIMapper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class OldTestOntologyModule extends OntologyModule {

	public OldTestOntologyModule(String config) {
		super(config);
	}
	
	public OldTestOntologyModule(String config, List<String> ignores) {
		super(config);
		setFileCacheIgnoreMappings(ignores);
	}

	@Provides
	@Named("PrimaryIRIMapper")
	@Singleton
	protected OWLOntologyIRIMapper getPrimary() {
		try {
			String catalogXml = "src/test/resources/ontologies/catalog-v001.xml";
			return new CatalogXmlIRIMapper(catalogXml);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}