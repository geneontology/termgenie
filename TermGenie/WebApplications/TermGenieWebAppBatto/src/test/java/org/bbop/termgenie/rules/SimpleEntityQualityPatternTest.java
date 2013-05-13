package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class SimpleEntityQualityPatternTest {

	private static TermGenerationEngine generationEngine;
	private static ConfiguredOntology batto;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_batto.xml", false, null),
				new XMLReloadingOntologyModule("ontology-configuration_batto.xml", 
						Arrays.asList("http://purl.obolibrary.org/obo/go/extensions/bio-attributes.owl", "http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo.owl"), null),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
		batto = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("Batto");
	}
	
	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntology(batto);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				// id: affects_quality , but label: 'has quality'
				assertNotNull(managed.getOWLObjectByLabel("has quality")); 
				assertNotNull(managed.getOWLObjectByIdentifier("affects_quality"));
				assertNotNull(managed.getOWLObjectByLabel("attribute of"));
				assertNotNull(managed.getOWLObjectByIdentifier("attribute_of"));
				
				assertNotNull(tool.parseManchesterExpression("'biological attribute'"));
				assertNotNull(tool.parseManchesterExpression("UBERON_0000120")); // blood brain barrier
				assertNotNull(tool.parseManchesterExpression("PATO_0000970")); // permeability
				
				OWLClassExpression expression = tool.parseManchesterExpression("'biological attribute' and 'has quality' some PATO_0000970 and 'attribute of' some UBERON_0000120");
				assertNotNull(expression);
				
				expression = tool.parseManchesterExpression("'biological attribute' and affects_quality some PATO_0000970 and attribute_of some UBERON_0000120");
				assertNotNull(expression);
				
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
		
	}

	@Test
	public void test_eq_simple_1() {
		
		List<TermGenerationInput> generationTasks = createEQSimpleTask("UBERON:0000120", "PATO:0000970"); // blood brain barrier permeability
		List<TermGenerationOutput> list = generationEngine.generateTerms(batto, generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		Frame term = output.getTerm();
		System.out.println(term);
		
		
	}

	private List<TermGenerationInput> createEQSimpleTask(String entity, String quality) {
		TermTemplate termTemplate = getEQSimple();
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField entityField = termTemplate.getFields().get(0);
		parameters.setTermValues(entityField.getName(), Arrays.asList(entity));
		
		TemplateField qualityField = termTemplate.getFields().get(1);
		parameters.setTermValues(qualityField.getName(), Arrays.asList(quality)); 
	
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		return generationTasks;
	}
	
	private TermTemplate getEQSimple() {
		return generationEngine.getAvailableTemplates().get(0);
	}
}
