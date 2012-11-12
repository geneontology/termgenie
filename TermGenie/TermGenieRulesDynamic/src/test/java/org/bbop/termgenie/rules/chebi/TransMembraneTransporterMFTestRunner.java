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

public class TransMembraneTransporterMFTestRunner {

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
	public void test_loaded_pattern() {
		TermTemplate template = getTransmembraneTransporterMFTemplate();
		List<TemplateField> fields = template.getFields();
		assertEquals(2, fields.size());
		TemplateField field = fields.get(0);
		assertEquals(4, field.getFunctionalPrefixes().size());
		assertArrayEquals(new String[]{"transmembrane transporter activity",
				"<b>secondary active</b> transmembrane transporter activity",
				"<b>uptake</b> transmembrane transporter activity",
				"<b>ATPase activity</b>, coupled to transmembrane movement of substances"}, field.getFunctionalPrefixes().toArray());
		assertEquals(4, field.getFunctionalPrefixesIds().size());
		assertArrayEquals(new String[]{"GO:0022857",
				"GO:0015291",
				"GO:0015563",
				"GO:0042626"}, field.getFunctionalPrefixesIds().toArray());
	}

	@Test
	public void test_transmembrane_transport() throws Exception {
		TermTemplate termTemplate = getTransmembraneTransporterMFTemplate();
		List<String> prefixIds = Arrays.asList("GO:0022857","GO:0015291","GO:0015563","GO:0042626");
		String id = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(4, list.size());
		
		TermGenerationOutput output1 = list.get(0);
		assertTrue(output1.getMessage(), output1.isSuccess());
		Frame term1 = output1.getTerm();
		renderFrame(term1);
		assertEquals("difenoxin transmembrane transporter activity", term1.getTagValue(OboFormatTag.TAG_NAME));
		
		TermGenerationOutput output2 = list.get(1);
		assertTrue(output2.getMessage(), output2.isSuccess());
		Frame term2 = output2.getTerm();
		renderFrame(term2);
		assertEquals("difenoxin secondary active transmembrane transporter activity", term2.getTagValue(OboFormatTag.TAG_NAME));
		
		TermGenerationOutput output3 = list.get(2);
		assertTrue(output3.getMessage(), output3.isSuccess());
		Frame term3 = output3.getTerm();
		renderFrame(term3);
		assertEquals("difenoxin uptake transmembrane transporter activity", term3.getTagValue(OboFormatTag.TAG_NAME));
		
		TermGenerationOutput output4 = list.get(3);
		assertTrue(output4.getMessage(), output4.isSuccess());
		Frame term4 = output4.getTerm();
		renderFrame(term4);
		assertEquals("difenoxin transmembrane-transporting ATPase activity", term4.getTagValue(OboFormatTag.TAG_NAME));
	}
	
	@Test
	public void test_equivalent_existing() throws Exception {
		TermTemplate termTemplate = getTransmembraneTransporterMFTemplate();
		List<String> prefixIds = Arrays.asList("GO:0022857");
		String id = "CHEBI:29988"; // L-glutamate(2−)
		
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertFalse(output1.isSuccess());
		assertEquals("Failed to create the term L-glutamate(2-) transmembrane transporter activity with the logical definition: \"GO_0022857 and 'transports or maintains localization of' some CHEBI_29988\" " +
				"The term GO:0005313 'L-glutamate transmembrane transporter activity' with the same logic definition already exists", output1.getMessage());
		
	}
	
	@Test
	public void test_equivalent_existing_via_gci() throws Exception {
		TermTemplate termTemplate = getTransmembraneTransporterMFTemplate();
		List<String> prefixIds = Arrays.asList("GO:0022857");
		String id = "CHEBI:29985"; // L-glutamate(1−)
		
		List<TermGenerationInput> generationTasks = createTransmembraneTransportTask(termTemplate, id, prefixIds);
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertFalse(output1.isSuccess());
		assertEquals("Failed to create the term L-glutamate(1-) transmembrane transporter activity with the logical definition: \"GO_0022857 and 'transports or maintains localization of' some CHEBI_29985\" " +
				"The term GO:0005313 'L-glutamate transmembrane transporter activity' with the same logic definition already exists", output1.getMessage());
		
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
	
	private TermTemplate getTransmembraneTransporterMFTemplate() {
		return generationEngine.getAvailableTemplates().get(3);
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
