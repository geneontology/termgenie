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
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.CatalogXmlIRIMapper;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;


public class TransportFromTo {
	
	static final class ChemicalTestOntologyModule extends XMLReloadingOntologyModule {

		ChemicalTestOntologyModule() {
			super("ontology-configuration_chemical.xml", null, null);
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
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_chemical.xml", false, true, null),
				new ChemicalTestOntologyModule(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("has_target_start_location"));
				assertNotNull(managed.getOWLObjectByLabel("has_target_end_location"));
				assertNotNull(managed.getOWLObjectByLabel("transports or maintains localization of"));
				
				assertNotNull(tool.parseManchesterExpression("GO_0006810"));
				assertNotNull(tool.parseManchesterExpression("CHEBI_4534"));
				assertNotNull(tool.parseManchesterExpression("GO_0005791"));
				assertNotNull(tool.parseManchesterExpression("GO_0005764"));
				
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0006810 and 'transports or maintains localization of' some CHEBI_4534 and 'has_target_start_location' some GO_0005791 and 'has_target_end_location' some GO_0005764");
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
	public void test_transport_from_to() throws Exception {
		TermTemplate template = getTransporterFromToTemplate();
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField fieldChemical = template.getFields().get(0);
		TemplateField fieldFrom = template.getFields().get(1);
		TemplateField fieldTo = template.getFields().get(2);
		String term = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		parameters.setTermValues(fieldChemical.getName(), Arrays.asList(term));
		parameters.setStringValues(fieldChemical.getName(), Arrays.asList("transport", "vesicle-mediated transport"));
	
		// from GO:0005791 ! rough endoplasmic reticulum
		parameters.setTermValues(fieldFrom.getName(), Arrays.asList("GO:0005791"));
		
		// to GO:0005764 ! lysosome
		parameters.setTermValues(fieldTo.getName(), Arrays.asList("GO:0005764"));
		
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(2, list.size());
		TermGenerationOutput output1 = list.get(0);
		TermGenerationOutput output2 = list.get(1);
		
		assertNull(output1.getError(), output1.getError());
		assertNull(output2.getError(), output2.getError());
		Frame frame1 = output1.getTerm();
		Frame frame2 = output2.getTerm();
//		renderFrame(frame1);
//		renderFrame(frame2);
		
		assertEquals("difenoxin transport from rough endoplasmic reticulum to lysosome", frame1.getTagValue(OboFormatTag.TAG_NAME));
		assertEquals("vesicle-mediated difenoxin transport from rough endoplasmic reticulum to lysosome", frame2.getTagValue(OboFormatTag.TAG_NAME));
	}
	
	@Test
	public void test_transport_from_to_fail() throws Exception {
		TermTemplate template = getTransporterFromToTemplate();
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField fieldChemical = template.getFields().get(0);
		String term = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		parameters.setTermValues(fieldChemical.getName(), Arrays.asList(term));
		parameters.setStringValues(fieldChemical.getName(), Arrays.asList("transport", "vesicle-mediated transport"));
	
		
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		
		assertNotNull(output1.getError());
		
	}
	
	private TermTemplate getTransporterFromToTemplate() {
		return generationEngine.getAvailableTemplates().get(10);
	}
}
