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
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class SimpleEntityQualityPatternTest {

	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void beforeClass() {
		OntologyModule ontologyModule = new OntologyModule("ontology-configuration_to.xml");
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_to.xml", false, true, true, null),
				ontologyModule,
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Ignore("Ontology is missing the imports")
	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("inheres in"));
				assertNotNull(managed.getOWLObjectByIdentifier("RO:0000052"));
				
				assertNotNull(tool.parseManchesterExpression("'plant trait'"));
				assertNotNull(tool.parseManchesterExpression("PO_0020123")); // root cap
				assertNotNull(tool.parseManchesterExpression("PATO_0000970")); // permeability
				
				OWLClassExpression expression = tool.parseManchesterExpression("PATO_0000970 and 'inheres in' some PO_0020123");
				assertNotNull(expression);
				
				expression = tool.parseManchesterExpression("PATO_0000970 and 'inheres in' some PO_0020123");
				assertNotNull(expression);
				
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
		
	}

	@Ignore("Ontology is missing the imports")
	@Test
	public void test_eq_simple_1() {
		
		List<TermGenerationInput> generationTasks = createEQSimpleTask("PO:0020123", "PATO:0000970"); // root cap permeability
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		// TODO verify output
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
