package org.bbop.termgenie.solr;

import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
import org.junit.BeforeClass;

public abstract class OntologyProvider {
	
	protected static Ontology go;
	protected static Ontology bp;
	protected static Ontology mf;
	protected static Ontology cc;
	protected static Ontology pro;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, ConfiguredOntology> ontologies = DefaultOntologyConfiguration.getOntologies();
		go = load("GeneOntology", ontologies);
		bp = load("biological_process", ontologies);
		mf = load("molecular_function", ontologies);
		cc = load("cellular_component", ontologies);
		pro = load("ProteinOntology", ontologies);
	}
	
	private static Ontology load(String name, Map<String, ConfiguredOntology> ontologies) {
		ConfiguredOntology ontology = ontologies.get(name);
		if (ontology.getRealInstance() != null) {
			return ontology;
		}
		return ontology.createOntology(DefaultOntologyLoader.getOntology(ontology));
	}
}
