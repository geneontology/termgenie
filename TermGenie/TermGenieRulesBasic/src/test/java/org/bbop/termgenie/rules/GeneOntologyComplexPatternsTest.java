package org.bbop.termgenie.rules;

import static org.bbop.termgenie.core.rules.DefaultTermTemplates.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public class GeneOntologyComplexPatternsTest {
	
	private static GeneOntologyComplexPatterns instance;
	private static OWLGraphWrapper go;
	private static OWLGraphWrapper pro;
	private static OWLGraphWrapper uberon;
	private static OWLGraphWrapper plant;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Map<String, ConfiguredOntology> config = DefaultOntologyConfiguration.getOntologies();
		go = load(GENE_ONTOLOGY, config);
		pro = load(PROTEIN_ONTOLOGY, config);
		uberon = load(UBERON_ONTOLOGY, config);
		plant = load(PLANT_ONTOLOGY, config);
		instance = new GeneOntologyComplexPatterns(go, pro, uberon, plant);
	}
	
	private static OWLGraphWrapper load(Ontology ontology, Map<String, ConfiguredOntology> config) {
		ConfiguredOntology configOntology = config.get(ontology.getUniqueName());
		OWLGraphWrapper owlGraphWrapper = DefaultOntologyLoader.getOntology(configOntology);
		configOntology.createOntology(owlGraphWrapper);
		return owlGraphWrapper;
	}

	@Test
	public void testGenerateTerms() {
		TermGenerationInput input1 = createRegulationBPExample1();
		List<TermGenerationOutput> output1 = instance.generateTerms(GENE_ONTOLOGY, Collections.singletonList(input1));
		assertEquals(3, output1.size());
		for (TermGenerationOutput termGenerationOutput : output1) {
			assertFalse("the requested terms are already in the ontology",
					termGenerationOutput.isSuccess());
			assertTrue(termGenerationOutput.getMessage().contains("same label already exists"));
		}
		// TODO add more test cases
	}

	@Test
	public void testGetDefXref() {
		TermGenerationInput input = createRegulationBPExample1();
		input.getParameters().getStrings().addValue("test input xref0", Field_DefX_Ref, 0);
		input.getParameters().getStrings().addValue("test input xref1", Field_DefX_Ref, 1);
		List<String> defXref = instance.getDefXref(input);
		assertArrayEquals(new String[]{"test input xref0", "test input xref1"}, defXref.toArray());
	}

	@Test
	public void testGetComment() {
		TermGenerationInput input = createInvoledInExample1();
		input.getParameters().getStrings().addValue("test input comment", Field_Comment, 0);
		String comment = instance.getComment(input);
		assertEquals("test input comment", comment);
	}

	@Test
	public void testCreateDefinition() {
		TermGenerationInput input = createInvoledInExample1();
		
		assertEquals("test gen def", instance.createDefinition("test gen def", input));
		
		input.getParameters().getStrings().addValue("test input def", Field_Definition, 0);
		
		assertEquals("test input def", instance.createDefinition("test gen def", input));
	}

	@Test
	public void testCreateName() {
		TermGenerationInput input = createInvoledInExample1();
		
		assertEquals("test gen name", instance.createName("test gen name", input));
		
		input.getParameters().getStrings().addValue("test input name", Field_Name, 0);
		
		assertEquals("test input name", instance.createName("test gen name", input));
	}

	
	// --------------- Helper methods ---------------
	
	private TermGenerationInput createRegulationBPExample1() {
		TermGenerationParameters parameters = new TermGenerationParameters();
		OntologyTerm term = create("GO:0048069", go); // eye pigmentation
		parameters.getTerms().addValue(term, Field_Target_Regulation_BP, 0);
		parameters.getPrefixes().addValue(Arrays.asList("regulation","negative_regulation","positive_regulation"), Field_Target_Regulation_BP, 0);
		TermGenerationInput input = new TermGenerationInput(all_regulation, parameters);
		return input;
	}

	private TermGenerationInput createInvoledInExample1() {
		TermGenerationParameters parameters = new TermGenerationParameters();
		OntologyTerm part = create("GO:0042440", go); // pigment metabolic process
		OntologyTerm whole = create("GO:0043473", go); // pigmentation
		parameters.getTerms().addValue(part, Field_Part_BP, 0);
		parameters.getTerms().addValue(whole, Field_Whole_BP, 0);
		TermGenerationInput input = new TermGenerationInput(involved_in, parameters);
		return input;
	}

	private static OntologyTerm create(String id, OWLGraphWrapper ontology) {
		OWLObject x = ontology.getOWLObjectByIdentifier(id);
		if (x != null) {
			String label = ontology.getLabel(x);
			return new OntologyTerm.DefaultOntologyTerm(id, label , null, null, null, null, null, null);
		}
		throw new RuntimeException("Unknown Id: "+id);
	}
}
