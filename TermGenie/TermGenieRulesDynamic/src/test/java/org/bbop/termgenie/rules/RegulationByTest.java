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
import org.bbop.termgenie.ontology.impl.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import com.google.inject.Injector;

public class RegulationByTest {

	private static TermGenerationEngine generationEngine;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_regulation_by.xml", false, true, null),
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
	}

	@Test
	public void test_regulation_by() throws Exception {
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:0051048")); // negative regulation of secretion
		parameters.setTermValues("regulator", Arrays.asList("GO:0090164")); // asymmetric Golgi ribbon formation

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError());
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
	@Ignore("The term already exists now.")
	public void test_regulation_by_synonyms_and_def1() throws Exception {
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:0070623")); // regulation of thiamine biosynthetic process
		parameters.setTermValues("regulator", Arrays.asList("GO:0006357")); // regulation of transcription from RNA polymerase II promoter

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError());
		Frame term = output.getTerm();

		assertEquals("regulation of thiamine biosynthetic process by regulation of transcription from RNA polymerase II promoter", 
				term.getTagValue(OboFormatTag.TAG_NAME, String.class));
		
		assertEquals("A regulation of transcription from RNA polymerase II promoter that results in regulation of thiamine biosynthetic process.", 
				term.getTagValue(OboFormatTag.TAG_DEF, String.class));
		
		Collection<Clause> synonyms = term.getClauses(OboFormatTag.TAG_SYNONYM);
		hasExactSynonyms(synonyms, "regulation of thiamine biosynthesis by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine anabolism by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine synthesis by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamin biosynthetic process by regulation of transcription from RNA polymerase II promoter",
				"regulation of thiamine formation by regulation of transcription from RNA polymerase II promoter");
	}
	
	@Test
	public void test_regulation_by_synonyms_and_def2() throws Exception {
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		parameters.setTermValues("process", Arrays.asList("GO:1901002")); // positive regulation of response to salt stress
		parameters.setTermValues("regulator", Arrays.asList("GO:0000122")); // negative regulation of transcription from RNA polymerase II promoter

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError());
		Frame term = output.getTerm();

		assertEquals("positive regulation of response to salt stress by negative regulation of transcription from RNA polymerase II promoter", 
				term.getTagValue(OboFormatTag.TAG_NAME, String.class));
		
		assertEquals("A negative regulation of transcription from RNA polymerase II promoter that results in positive regulation of response to salt stress.", 
				term.getTagValue(OboFormatTag.TAG_DEF, String.class));
		
		Collection<Clause> synonyms = term.getClauses(OboFormatTag.TAG_SYNONYM);
		int count = hasExactSynonyms(synonyms, 
				"activation of response to ionic osmotic stress by negative regulation of transcription from RNA polymerase II promoter",
				"activation of salinity response by negative regulation of transcription from RNA polymerase II promoter",
				"positive regulation of response to ionic osmotic stress by negative regulation of transcription from RNA polymerase II promoter",
				"positive regulation of salinity response by negative regulation of transcription from RNA polymerase II promoter",
				"up regulation of salinity response by negative regulation of transcription from RNA polymerase II promoter",
				"up regulation of response to ionic osmotic stress by negative regulation of transcription from RNA polymerase II promoter",
				"up regulation of response to salt stress by negative regulation of transcription from RNA polymerase II promoter",
				"up-regulation of response to ionic osmotic stress by negative regulation of transcription from RNA polymerase II promoter", 
				"up-regulation of response to salt stress by negative regulation of transcription from RNA polymerase II promoter", 
				"up-regulation of salinity response by negative regulation of transcription from RNA polymerase II promoter", 
				"upregulation of response to ionic osmotic stress by negative regulation of transcription from RNA polymerase II promoter", 
				"upregulation of response to salt stress by negative regulation of transcription from RNA polymerase II promoter", 
				"upregulation of salinity response by negative regulation of transcription from RNA polymerase II promoter"); 
		count += hasSynonyms(synonyms, OboFormatTag.TAG_NARROW, 
				"activation of response to salt stress by negative regulation of transcription from RNA polymerase II promoter");
		assertEquals(14, count);
	}
	
	private int hasExactSynonyms(Collection<Clause> synonyms, String...labels) {
		return hasSynonyms(synonyms, OboFormatTag.TAG_EXACT, labels);
	}
	
	private int hasSynonyms(Collection<Clause> synonyms, OboFormatTag tag, String...labels) {
		final String tagString = tag.getTag();
		for (String label : labels) {
			boolean found = false;
			for(Clause clause : synonyms) {
				final Object synLabel = clause.getValue();
				final Object scope = clause.getValue2();
				if (label.equals(synLabel) && tagString.equals(scope)) {
					found = true;
					break;
				}
			}
			assertTrue("Did not find label: '"+label+"' and tag: "+tagString, found);
		}
		return labels.length;
	}
}
