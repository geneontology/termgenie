package org.bbop.termgenie.rules.chebi;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ChemicalTermGenieScriptCatalogXmlTestRunner {

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
	public void testOWL2OBO() throws Exception {
		
		OntologyTaskManager ontologyManager = loader.getOntology(go);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				Owl2Obo owl2Obo = new Owl2Obo();
				OBODoc oboDoc = owl2Obo.convert(managed.getSourceOntology());
				OBOFormatWriter writer = new OBOFormatWriter();
				StringWriter stringWriter = new StringWriter();
				writer.write(oboDoc, new BufferedWriter(stringWriter));
				
				String oboString = stringWriter.getBuffer().toString();
				assertNotNull(oboString);
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
	}
	
	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntology(go);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("has participant"));
				assertNotNull(managed.getOWLObjectByLabel("has input"));
				assertNotNull(managed.getOWLObjectByLabel("has output"));
				assertNotNull(managed.getOWLObjectByLabel("transports or maintains localization of"));
				
				assertNotNull(tool.parseManchesterExpression("CHEBI_16947"));
				assertNotNull(tool.parseManchesterExpression("GO_0008152"));
				
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0008152 and 'has participant' some CHEBI_16947");
				assertNotNull(expression);
				
				expression = tool.parseManchesterExpression("GO_0008152 and 'has input' some CHEBI_16947");
				assertNotNull(expression);
				
				expression = tool.parseManchesterExpression("GO_0008152 and 'has output' some CHEBI_16947");
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
		assertEquals("Failed to create the term citrate(3-) metabolic process with the logical definition: \"GO_0008152 and 'has participant' some CHEBI_16947\" " +
				"The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
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
		assertEquals("Failed to create the term citrate(2-) metabolic process with the logical definition: \"GO_0008152 and 'has participant' some CHEBI_35808\" " +
				"The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
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
		assertEquals("Failed to create the term citrate(1-) metabolic process with the logical definition: \"GO_0008152 and 'has participant' some CHEBI_35804\" " +
				"The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
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
		assertEquals("Failed to create the term citric acid metabolic process with the logical definition: \"GO_0008152 and 'has participant' some CHEBI_30769\" " +
				"The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
		
		
	}
	
	
	@Test
	public void test_chebi_difenoxin() {
		TermTemplate termTemplate = getMetabolismTemplate();
		List<String> prefixes = Arrays.asList("metabolism", "catabolism", "biosynthesis");
		String id = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		List<TermGenerationInput> generationTasks = createMetabolismTask(termTemplate, id, prefixes); 
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(3, list.size());
		TermGenerationOutput output1 = list.get(0);
		TermGenerationOutput output2 = list.get(1);
		TermGenerationOutput output3 = list.get(2);
		
		assertTrue(output1.getMessage(), output1.isSuccess());
		Frame term1 = output1.getTerm();
		assertEquals("difenoxin metabolic process", term1.getTagValue(OboFormatTag.TAG_NAME));
		containsSynonym(term1, "difenoxin metabolism");

		assertTrue(output2.getMessage(), output2.isSuccess());
		Frame term2 = output2.getTerm();
		assertEquals("difenoxin catabolic process", term2.getTagValue(OboFormatTag.TAG_NAME));
		containsSynonym(term2, "difenoxin catabolism", 
				"difenoxin breakdown", 
				"difenoxin degradation");
		
		assertTrue(output3.getMessage(), output3.isSuccess());
		Frame term3 = output3.getTerm();
		assertEquals("difenoxin biosynthetic process", term3.getTagValue(OboFormatTag.TAG_NAME));
		containsSynonym(term3, "difenoxin biosynthesis", 
				"difenoxin anabolism",
				"difenoxin formation",
				"difenoxin synthesis");
	}
	
	private void containsSynonym(Frame frame, String...synonymsStrings) {
		Set<String> requiredSynonyms = new HashSet<String>(Arrays.asList(synonymsStrings));
		Collection<Clause> synonyms = frame.getClauses(OboFormatTag.TAG_SYNONYM);
		System.out.println("-----------------");
		for (Clause clause : synonyms) {
			String value = clause.getValue(String.class);
			requiredSynonyms.remove(value);
			System.out.println(value+" "+clause.getValue2());
		}
		if (!requiredSynonyms.isEmpty()) {
			fail("Missing synonyms: "+requiredSynonyms);
		}
	}

	@Test
	public void test_transport() throws Exception {
		TermTemplate template = getChemicalTransportTemplate();
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = template.getFields().get(0);
		String term = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		parameters.setTermValues(field.getName(), Arrays.asList(term )); 
	
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertTrue(output.getMessage(), output.isSuccess());
		Frame frame = output.getTerm();
		renderFrame(frame);
		
		assertEquals("difenoxin transport", frame.getTagValue(OboFormatTag.TAG_NAME));
		assertEquals("biological_process", frame.getTagValue(OboFormatTag.TAG_NAMESPACE));
		Collection<String> values = frame.getTagValues(OboFormatTag.TAG_IS_A, String.class);
		String[] array = values.toArray(new String[values.size()]);
		Arrays.sort(array);
		// monocarboxylic acid transport
		// amine transport
		assertArrayEquals(new String[]{"GO:0015718","GO:0015837"}, array);
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
	
	@Test
	public void test_binding() throws Exception {
		TermTemplate template = getChemicalBindingTemplate();
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = template.getFields().get(0);
		String term = "CHEBI:35808"; // citrate(2-)
		parameters.setTermValues(field.getName(), Arrays.asList(term )); 
	
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertTrue(output.getMessage(), output.isSuccess());
		Frame frame = output.getTerm();
		renderFrame(frame);
		
		assertEquals("citrate(2-) binding", frame.getTagValue(OboFormatTag.TAG_NAME));
		assertEquals("molecular_function", frame.getTagValue(OboFormatTag.TAG_NAMESPACE));
		assertEquals("GO:0031406", frame.getTagValue(OboFormatTag.TAG_IS_A)); // carboxylic acid binding
		
	}
	
	private TermTemplate getMetabolismTemplate() {
		return generationEngine.getAvailableTemplates().get(0);
	}
	
	private TermTemplate getChemicalTransportTemplate() {
		return generationEngine.getAvailableTemplates().get(1);
	}
	
	private TermTemplate getChemicalBindingTemplate() {
		return generationEngine.getAvailableTemplates().get(2);
	}

	private List<TermGenerationInput> createMetabolismTask(TermTemplate termTemplate, final String term) {
		return createMetabolismTask(termTemplate, term, Arrays.asList("metabolism"));
	}
	
	private List<TermGenerationInput> createMetabolismTask(TermTemplate termTemplate, final String term, List<String> prefixes) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = termTemplate.getFields().get(0);
		parameters.setTermValues(field.getName(), Arrays.asList(term)); 
		parameters.setStringValues(field.getName(), prefixes);
	
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		return generationTasks;
	}

}
