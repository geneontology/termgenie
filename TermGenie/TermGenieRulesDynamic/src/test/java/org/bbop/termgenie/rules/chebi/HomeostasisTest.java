package org.bbop.termgenie.rules.chebi;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
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
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class HomeostasisTest {

	public static final class ChemicalTestOntologyModule extends XMLReloadingOntologyModule {

		public ChemicalTestOntologyModule() {
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
	private static ConfiguredOntology go;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_chemical.xml", false, null),
				new ChemicalTestOntologyModule(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
		go = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("GeneOntology");
	}
	
	@Test
	public void test_homeostasis() throws Exception {
		TermTemplate termTemplate = getTemplate();
		List<String> prefixIds = Arrays.asList("GO:0048878","GO:0055082");
		String id = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, false, null);
		assertNotNull(list);
		assertEquals(2, list.size());
		
		TermGenerationOutput output1 = list.get(0);
		assertNull(output1.getError());
		Frame term1 = output1.getTerm();
		renderFrame(term1);
		assertEquals("difenoxin homeostasis", term1.getTagValue(OboFormatTag.TAG_NAME));
		
		TermGenerationOutput output2 = list.get(1);
		assertNull(output2.getError());
		Frame term2 = output2.getTerm();
		renderFrame(term2);
		assertEquals("cellular difenoxin homeostasis", term2.getTagValue(OboFormatTag.TAG_NAME));
		
	}
	
	@Test
	public void test_equivalent_existing1() throws Exception {
		TermTemplate termTemplate = getTemplate();
		List<String> prefixIds = Arrays.asList("GO:0048878");
		String id = "CHEBI:15377"; // water
		
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertEquals("The term GO:0030104 with the same label 'water homeostasis' already exists", output1.getError());
		
	}
	
	@Test
	public void test_equivalent_existing2() throws Exception {
		TermTemplate termTemplate = getTemplate();
		List<String> prefixIds = Arrays.asList("GO:0048878");
		String id = "CHEBI:29412"; // oxonium  H3O+
		
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertEquals("Failed to create the term oxonium homeostasis with the logical definition: \"GO_0048878 and 'regulates level of' some CHEBI_29412\" " +
				"The term GO:0030104 'water homeostasis' with the same logic definition already exists", output1.getError());
		
	}
	
	private void renderFrame(final Frame frame) throws InvalidManagedInstanceException  {
		OntologyTaskManager ontologyManager = loader.getOntology(go);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				NameProvider provider = new  OwlGraphWrapperNameProvider(managed);
				String obo = OboWriterTools.writeFrame(frame, provider);
				System.out.println("-----------");
				System.out.println(obo);
				System.out.println("-----------");
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
	}
	
	private TermTemplate getTemplate() {
		return generationEngine.getAvailableTemplates().get(5);
	}

	private List<TermGenerationInput> createTransmembraneTransportTask(TermTemplate termTemplate, final String term, List<String> prefixIds) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = termTemplate.getFields().get(0);
		parameters.setTermValues(field.getName(), Arrays.asList(term)); 
		parameters.setStringValues(field.getName(), prefixIds);
	
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		return generationTasks;
	}

}
