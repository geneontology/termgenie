package org.bbop.termgenie.solr;

import java.util.Map;

import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.BeforeClass;

public abstract class OntologyProvider {
	
	protected static OntologyTaskManager go;
	protected static OntologyTaskManager bp;
	protected static OntologyTaskManager mf;
	protected static OntologyTaskManager cc;
	protected static OntologyTaskManager pro;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, ConfiguredOntology> ontologies = DefaultOntologyConfiguration.getOntologies();
		go = load("GeneOntology", ontologies);
		bp = load("biological_process", ontologies);
		mf = load("molecular_function", ontologies);
		cc = load("cellular_component", ontologies);
		pro = load("ProteinOntology", ontologies);
	}
	
	private static OntologyTaskManager load(String name, Map<String, ConfiguredOntology> ontologies) {
		ConfiguredOntology ontology = ontologies.get(name);
		return DefaultOntologyLoader.getOntology(ontology);
	}
}
