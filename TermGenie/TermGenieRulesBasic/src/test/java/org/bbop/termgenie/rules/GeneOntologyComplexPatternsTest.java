package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class GeneOntologyComplexPatternsTest {

	private static GeneOntologyComplexPatterns instance;
	private static OWLGraphWrapper go;
	private static OWLGraphWrapper pro;
	private static OWLGraphWrapper uberon;
	private static OWLGraphWrapper plant;
	private static OntologyLoader loader;
	private static DefaultTermTemplates templates;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new DefaultOntologyModule(),
				new IOCModule() {

					@Override
					protected void configure() {
						bind(DefaultTermTemplates.class);
					}
				},
				new ReasonerModule());
		OntologyConfiguration config = injector.getInstance(OntologyConfiguration.class);
		templates = injector.getInstance(DefaultTermTemplates.class);
		Map<String, ConfiguredOntology> ontologies = config.getOntologyConfigurations();
		loader = injector.getInstance(OntologyLoader.class);
		go = load(templates.GENE_ONTOLOGY, ontologies);
		pro = load(templates.PROTEIN_ONTOLOGY, ontologies);
		uberon = load(templates.UBERON_ONTOLOGY, ontologies);
		plant = load(templates.PLANT_ONTOLOGY, ontologies);
		ReasonerFactory factory = injector.getInstance(ReasonerFactory.class);
		instance = new GeneOntologyComplexPatterns(Arrays.asList(go, pro, uberon, plant), templates, factory);
	}

	private static OWLGraphWrapper load(Ontology ontology, Map<String, ConfiguredOntology> config) {
		ConfiguredOntology configOntology = config.get(ontology.getUniqueName());
		OntologyTaskManager manager = loader.getOntology(configOntology);
		OntologyTaskImplementation task = new OntologyTaskImplementation();
		manager.runManagedTask(task);
		return task.wrapper;
	}

	private static final class OntologyTaskImplementation implements
			OntologyTaskManager.OntologyTask
	{

		OWLGraphWrapper wrapper = null;

		@Override
		public Modified run(OWLGraphWrapper managed) {
			wrapper = managed;
			return Modified.no;
		}
	}

	@Test
	public void testGenerateTerms() {
		TermGenerationInput input1 = createRegulationBPExample1();
		List<TermGenerationOutput> output1 = instance.generateTerms(templates.GENE_ONTOLOGY,
				Collections.singletonList(input1));
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
		input.getParameters().getStrings()[templates.all_regulation.getFieldPos("DefX_Ref")] = new String[] { "test input xref0",
				"test input xref1" };
		List<String> defXref = instance.getDefXref(input);
		assertArrayEquals(new String[] { "test input xref0", "test input xref1" },
				defXref.toArray());
	}

	@Test
	public void testGetComment() {
		TermGenerationInput input = createInvoledInExample1();
		input.getParameters().getStrings()[templates.involved_in.getFieldPos("Comment")] = new String[] { "test input comment" };
		String comment = instance.getComment(input);
		assertEquals("test input comment", comment);
	}

	@Test
	public void testCreateDefinition() {
		TermGenerationInput input = createInvoledInExample1();

		assertEquals("test gen def", instance.createDefinition("test gen def", input));

		input.getParameters().getStrings()[templates.involved_in.getFieldPos("Definition")] = new String[] { "test input def" };

		assertEquals("test input def", instance.createDefinition("test gen def", input));
	}

	@Test
	public void testCreateName() {
		TermGenerationInput input = createInvoledInExample1();

		assertEquals("test gen name", instance.createName("test gen name", input));

		input.getParameters().setStringValues(templates.involved_in, "Name", "test input name");

		assertEquals("test input name", instance.createName("test gen name", input));
	}

	// --------------- Helper methods ---------------

	private TermGenerationInput createRegulationBPExample1() {
		TermTemplate template = templates.all_regulation;
		TermGenerationParameters parameters = new TermGenerationParameters(template.getFieldCount());

		OntologyTerm term = create("GO:0048069", go); // eye pigmentation;
		parameters.setTermValues(template, "target", term);
		parameters.setStringValues(template,
				"target",
				"regulation",
				"negative_regulation",
				"positive_regulation");

		TermGenerationInput input = new TermGenerationInput(template, parameters);
		return input;
	}

	private TermGenerationInput createInvoledInExample1() {
		TermTemplate template = templates.involved_in;
		TermGenerationParameters parameters = new TermGenerationParameters(template.getFieldCount());
		OntologyTerm part = create("GO:0042440", go); // pigment metabolic
														// process
		OntologyTerm whole = create("GO:0043473", go); // pigmentation

		parameters.setTermValues(template, "part", part);
		parameters.setTermValues(template, "whole", whole);

		TermGenerationInput input = new TermGenerationInput(template, parameters);
		return input;
	}

	private static OntologyTerm create(String id, OWLGraphWrapper ontology) {
		OWLObject x = ontology.getOWLObjectByIdentifier(id);
		if (x != null) {
			String label = ontology.getLabel(x);
			if (label != null) {
				return new OntologyTerm.DefaultOntologyTerm(id, label, null, null, null, null, null);
			}
		}
		throw new RuntimeException("Unknown Id: " + id);
	}
}
