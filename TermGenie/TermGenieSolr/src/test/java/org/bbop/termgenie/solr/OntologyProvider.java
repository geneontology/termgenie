package org.bbop.termgenie.solr;

import java.util.Map;

import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.DefaultTermTemplatesModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class OntologyProvider {
	
	protected static OntologyTaskManager go;
	protected static OntologyTaskManager bp;
	protected static OntologyTaskManager mf;
	protected static OntologyTaskManager cc;
	protected static OntologyTaskManager pro;
	private static OntologyLoader loader;
	protected static DefaultTermTemplates templates;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = Guice.createInjector(new DefaultOntologyModule(), new DefaultTermTemplatesModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		Map<String, ConfiguredOntology> ontologies = configuration.getOntologyConfigurations();
		loader = injector.getInstance(OntologyLoader.class);
		templates = injector.getInstance(DefaultTermTemplates.class);
		go = load("GeneOntology", ontologies);
		bp = load("biological_process", ontologies);
		mf = load("molecular_function", ontologies);
		cc = load("cellular_component", ontologies);
		pro = load("ProteinOntology", ontologies);
	}
	
	private static OntologyTaskManager load(String name, Map<String, ConfiguredOntology> ontologies) {
		ConfiguredOntology ontology = ontologies.get(name);
		return loader.getOntology(ontology);
	}
}
