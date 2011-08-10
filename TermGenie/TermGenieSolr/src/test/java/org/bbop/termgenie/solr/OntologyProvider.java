package org.bbop.termgenie.solr;

import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class OntologyProvider {
	
	protected static OntologyTaskManager goManager;
	protected static OntologyTaskManager proManager;
	
	protected static Ontology go;
	protected static Ontology bp;
	protected static Ontology mf;
	protected static Ontology cc;
	protected static Ontology pro;
	
	private static OntologyLoader loader;
	
	protected static ReasonerFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = Guice.createInjector(new DefaultOntologyModule(), new ReasonerModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		Map<String, ConfiguredOntology> ontologies = configuration.getOntologyConfigurations();
		loader = injector.getInstance(OntologyLoader.class);
		factory = injector.getInstance(ReasonerFactory.class);
		
		go = ontologies.get("GeneOntology");
		bp = ontologies.get("biological_process");
		mf = ontologies.get("molecular_function");
		cc = ontologies.get("cellular_component");
		pro = ontologies.get("ProteinOntology");
		
		goManager = load("GeneOntology", ontologies);
		proManager = load("ProteinOntology", ontologies);
	}
	
	private static OntologyTaskManager load(String name, Map<String, ConfiguredOntology> ontologies) {
		ConfiguredOntology ontology = ontologies.get(name);
		return loader.getOntology(ontology);
	}
}
