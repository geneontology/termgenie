package org.bbop.termgenie.rules.chebi;

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
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.CatalogXmlIRIMapper;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ChemicalTermGenieScriptCatalogXmlTestRunner {

	static final class ChemicalTestOntologyModule extends DefaultOntologyModule {

		ChemicalTestOntologyModule() {
			super(null);
		}

		@Override
		protected void bindOntologyConfiguration() {
			bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
			bind("XMLOntologyConfigurationResource",
					"ontology-configuration_chemical.xml");
		}

		@Override
		protected void bindIRIMapper() {
			// do nothing, use @Provides instead
		}
	
		@Singleton
		@Provides
		protected IRIMapper provideIRIMapper() {
			String catalogXml = "src/test/resources/ontologies/catalog-v001.xml";
			return new CatalogXmlIRIMapper(null, catalogXml);
		}
	}

	private static TermGenerationEngine generationEngine;
	private static ConfiguredOntology go;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_chemical.xml", null),
				new ChemicalTestOntologyModule(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
		go = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("GeneOntology");
	}
	
	@Test
	public void testManchesterSyntaxTool() {
		OntologyTaskManager ontologyManager = loader.getOntology(go);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("has participant"));
				
				assertNotNull(tool.parseManchesterExpression("CHEBI_16947"));
				assertNotNull(tool.parseManchesterExpression("GO_0008152"));
				
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0008152 and 'has participant' some CHEBI_16947");
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
	public void test_metabolism_citrate3() {
		TermTemplate termTemplate = getMetabolismTemplate();
		List<TermGenerationInput> generationTasks = createMetabolismTask(termTemplate, "CHEBI:16947"); // citrate(3-)
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertFalse(output.isSuccess());
		assertEquals("The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
	}

	@Test
	public void test_metabolism_citrate2() {
		TermTemplate termTemplate = getMetabolismTemplate();
		List<TermGenerationInput> generationTasks = createMetabolismTask(termTemplate, "CHEBI:35808"); // citrate(2-)
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertFalse(output.isSuccess());
		assertEquals("The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
	}
	
	@Test
	public void test_metabolism_citrate1() {
		TermTemplate termTemplate = getMetabolismTemplate();
		List<TermGenerationInput> generationTasks = createMetabolismTask(termTemplate, "CHEBI:35804"); // citrate(1-)
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertFalse(output.isSuccess());
		assertEquals("The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
	}
	
	@Test
	public void test_metabolism_citric_acid() {
		TermTemplate termTemplate = getMetabolismTemplate();
		List<TermGenerationInput> generationTasks = createMetabolismTask(termTemplate, "CHEBI:30769"); // citric acid
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertFalse(output.isSuccess());
		assertEquals("The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
	}

	private TermTemplate getMetabolismTemplate() {
		return generationEngine.getAvailableTemplates().get(0);
	}

	private List<TermGenerationInput> createMetabolismTask(TermTemplate termTemplate, final String term) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = termTemplate.getFields().get(0);
		parameters.setTermValues(field.getName(), Arrays.asList(term)); 
		parameters.setStringValues(field.getName(), Arrays.asList("metabolism"));
	
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		return generationTasks;
	}

}
