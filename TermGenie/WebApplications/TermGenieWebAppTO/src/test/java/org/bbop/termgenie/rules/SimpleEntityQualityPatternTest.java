package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.servlets.ToOntologyHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class SimpleEntityQualityPatternTest {

	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;

	private static File workFolder;
	
	private static IOCModule getOntologyModule() throws IOException {
		File tempFile = File.createTempFile("junit", "", FileUtils.getTempDirectory()).getCanonicalFile();
		FileUtils.deleteQuietly(tempFile);
		workFolder = new File(tempFile.getParentFile(), tempFile.getName()+"-folder");
		workFolder.mkdirs();
		IOCModule m = ToOntologyHelper.createDefaultOntologyModule(workFolder.getAbsolutePath(), null);
		return m;
	}
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		boolean filterNonAsciiSynonyms = false; // allow japanese synonyms
		IOCModule ontologyModule = getOntologyModule();
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_to.xml", false, true, filterNonAsciiSynonyms, null),
				ontologyModule,
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		FileUtils.deleteDirectory(workFolder);
	}
	
	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("inheres in"));
				assertNotNull(managed.getOWLObjectByIdentifier("inheres_in"));
				assertNotNull(managed.getOWLObjectByIdentifier("RO:0000052"));
				
				assertNotNull(tool.parseManchesterExpression("'plant trait'"));
				assertNotNull(tool.parseManchesterExpression("PO_0020123")); // root cap
				assertNotNull(tool.parseManchesterExpression("PATO_0000970")); // permeability
				
				OWLClassExpression expression = tool.parseManchesterExpression("PATO_0000970 and 'inheres in' some PO_0020123");
				assertNotNull(expression);
				
				expression = tool.parseManchesterExpression("PATO_0000970 and RO_0000052 some PO_0020123");
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
		
		List<TermGenerationInput> generationTasks = createEQSimpleTask("PO:0020123", "PATO:0000970"); // root cap permeability
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		Frame term = output.getTerm();
		assertEquals("root cap permeability", term.getTagValue(OboFormatTag.TAG_NAME));
		Collection<Clause> synonymClauses = term.getClauses(OboFormatTag.TAG_SYNONYM);
		System.out.println(synonymClauses.size());
		for (Clause clause : synonymClauses) {
			System.out.println(clause);
		}
		assertFalse(synonymClauses.isEmpty());
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
