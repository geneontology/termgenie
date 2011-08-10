package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class DefaultOntologyConfigurationTest {

	@Test
	public void testGetOntologyConfigurations() {
		DefaultOntologyConfiguration d = new DefaultOntologyConfiguration("default-ontology-configuration.settings");
		Map<String, ConfiguredOntology> configurations = d.getOntologyConfigurations();
		assertEquals(12, configurations.size());
		assertTrue(configurations.containsKey("GeneOntology"));
		assertTrue(configurations.containsKey("biological_process"));
		assertTrue(configurations.containsKey("molecular_function"));
		assertTrue(configurations.containsKey("cellular_component"));
		assertTrue(configurations.containsKey("ProteinOntology"));
		assertTrue(configurations.containsKey("Uberon"));
		assertTrue(configurations.containsKey("HumanPhenotype"));
		assertTrue(configurations.containsKey("FMA"));
		assertTrue(configurations.containsKey("PATO"));
		assertTrue(configurations.containsKey("OMP"));
		assertTrue(configurations.containsKey("CL"));
		assertTrue(configurations.containsKey("PO"));
		
		ConfiguredOntology go = configurations.get("GeneOntology");
		assertEquals("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology_ext.obo", go.source);
		assertArrayEquals(new String[]{"GO:0008150", "GO:0003674", "GO:0005575"}, go.getRoots().toArray(new String[0]));
		assertArrayEquals(new String[]{"ProteinOntology", "Uberon", "PO"}, go.requires.toArray(new String[0]));
		assertEquals(3, go.supports.size());
		
		ConfiguredOntology bp = configurations.get("biological_process");
		assertEquals("http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology_ext.obo", bp.source);
		assertArrayEquals(new String[]{"GO:0008150"}, bp.getRoots().toArray(new String[0]));
	}

}
