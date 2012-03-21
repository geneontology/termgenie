package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import com.google.inject.Injector;

public class TermGenieScriptTestRunnerRegulationBy {

	private static TermGenerationEngine generationEngine;
	private static OntologyConfiguration configuration;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_regulation_by.xml", null),
				new TestDefaultOntologyModule() {

					@Override
					protected void bindOntologyConfiguration() {
						bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
						bind("XMLOntologyConfigurationResource",
								"ontology-configuration_regulation_by.xml");
					}
				},
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		configuration = injector.getInstance(OntologyConfiguration.class);
	}

	@Test
	public void test_regulation_by() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:0051048")); // negative regulation of secretion
		parameters.setTermValues("regulator", Arrays.asList("GO:0090164")); // asymmetric Golgi ribbon formation

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertTrue(output.getMessage(), output.isSuccess());
		Frame term = output.getTerm();

		assertEquals("negative regulation of secretion by asymmetric Golgi ribbon formation", term.getTagValue(OboFormatTag.TAG_NAME, String.class));
		
		assertTrue(term.getClauses(OboFormatTag.TAG_SYNONYM).isEmpty());
		
		List<Clause> clauses = OboTools.getRelations(term);
		
		assertEquals(5, clauses.size());
		Clause clause0 = clauses.get(0);
		assertEquals(OboFormatTag.TAG_IS_A.getTag(), clause0.getTag());
		assertEquals("GO:0051048", clause0.getValue());
		
		Clause clause1 = clauses.get(1);
		assertEquals(OboFormatTag.TAG_IS_A.getTag(), clause1.getTag());
		assertEquals("GO:0090165", clause1.getValue());

		Clause clause2 = clauses.get(2);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause2.getTag());
		assertEquals("GO:0090164", clause2.getValue());

		Clause clause3 = clauses.get(3);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause3.getTag());
		assertEquals("results_in", clause3.getValue());
		assertEquals("GO:0051048", clause3.getValue2());

		Clause clause4 = clauses.get(4);
		assertEquals(OboFormatTag.TAG_RELATIONSHIP.getTag(), clause4.getTag());
		assertEquals("results_in", clause4.getValue());
		assertEquals("GO:0051048", clause4.getValue2());
	}

	@Test
	public void test_regulation_by_synonyms1() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:0070623")); // regulation of thiamine biosynthetic process
		parameters.setTermValues("regulator", Arrays.asList("GO:0006357")); // regulation of transcription from RNA polymerase II promoter

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertTrue(output.getMessage(), output.isSuccess());
		Frame term = output.getTerm();

		assertEquals("regulation of thiamine biosynthetic process by regulation of transcription from RNA polymerase II promoter", 
				term.getTagValue(OboFormatTag.TAG_NAME, String.class));
		
		Collection<Clause> synonyms = term.getClauses(OboFormatTag.TAG_SYNONYM);
		hasExactSynonyms(synonyms, "regulation of thiamine biosynthesis by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine anabolism by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine synthesis by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamin biosynthetic process by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine formation by regulation of transcription from RNA polymerase II promoter");
	}
	
	@Test
	public void test_regulation_by_synonyms2() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:1900070")); // negative regulation of cellular hyperosmotic salinity response
		parameters.setTermValues("regulator", Arrays.asList("GO:0000122")); // negative regulation of transcription from RNA polymerase II promoter

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertTrue(output.getMessage(), output.isSuccess());
		Frame term = output.getTerm();

		assertEquals("negative regulation of cellular hyperosmotic salinity response by negative regulation of transcription from RNA polymerase II promoter", 
				term.getTagValue(OboFormatTag.TAG_NAME, String.class));
		
		Collection<Clause> synonyms = term.getClauses(OboFormatTag.TAG_SYNONYM);
		hasExactSynonyms(synonyms, "negative regulation of cellular response to hyperosmotic salt stress by negative regulation of transcription from RNA polymerase II promoter");
	}
	
	private void hasExactSynonyms(Collection<Clause> synonyms, String...labels) {
		assertEquals(labels.length, synonyms.size());
		for (String label : labels) {
			boolean found = false;
			for(Clause clause : synonyms) {
				final Object synLabel = clause.getValue();
				final Object scope = clause.getValue2();
				if (label.equals(synLabel) && OboFormatTag.TAG_EXACT.getTag().equals(scope)) {
					found = true;
					break;
				}
			}
			assertTrue("Did not find label: "+label, found);
		}
	}
}
