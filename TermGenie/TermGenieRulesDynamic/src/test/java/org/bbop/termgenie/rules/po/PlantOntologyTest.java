package org.bbop.termgenie.rules.po;

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
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class PlantOntologyTest {

	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void beforeClass() {
		List<String> ignoreMappings = Arrays.asList("http://purl.obolibrary.org/obo/po/releases/2013-05-27/po.owl", "http://purl.obolibrary.org/obo/go/extensions/gene_ontology_xp.owl");
		
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_plant.xml", false, null),
				new XMLReloadingOntologyModule("ontology-configuration_plant.xml", ignoreMappings, null),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	
	@Test
	public void testPlantDevelopment() throws Exception {
		String term = "PO:0025130"; // cigar leaf
		TermGenerationOutput output = generateSingle(term , getPlantDevelopmentTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
		
	}
	
//	@Test
//	public void testPlantDevelopment2() throws Exception {
//		String term = "PO:0009001"; // fruit
//		TermGenerationOutput output = generateSingle(term , getPlantDevelopmentTemplate());
//		Frame frame = output.getTerm();
//		renderFrame(frame);
//		
//	}
//	
//	@Test
//	public void testPlantDevelopment3() throws Exception {
//		String term = "PO:0025281"; // pollen
//		TermGenerationOutput output = generateSingle(term , getPlantDevelopmentTemplate());
//		Frame frame = output.getTerm();
//		renderFrame(frame);
//		
//	}
	
	@Test
	public void testPlantFormation() throws Exception {
		String term = "PO:0025130"; // cigar leaf
		TermGenerationOutput output = generateSingle(term , getPlantFormationTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	@Test
	public void testPlantMaturation() throws Exception {
		String term = "PO:0025130"; // cigar leaf
		TermGenerationOutput output = generateSingle(term , getPlantMaturationTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	@Test
	public void testPlantMorphogenesis() throws Exception {
		String term = "PO:0025130"; // cigar leaf
		TermGenerationOutput output = generateSingle(term , getPlantMorphogenesisTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	@Test
	public void testPlantStructuralOrganization() throws Exception {
		String term = "PO:0025130"; // cigar leaf
		TermGenerationOutput output = generateSingle(term , getPlantStructuralOrganizationTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	@Test
	public void testPlantStructuralOrganization2() throws Exception {
		String term = "PO:0009001"; // fruit
		TermGenerationOutput output = generateSingle(term , getPlantStructuralOrganizationTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	@Test
	public void testPlantStructuralOrganization3() throws Exception {
		String term = "PO:0025281"; // pollen
		TermGenerationOutput output = generateSingle(term , getPlantStructuralOrganizationTemplate());
		Frame frame = output.getTerm();
		renderFrame(frame);
	}
	
	private TermGenerationOutput generateSingle(String term, TermTemplate template) {
		List<TermGenerationOutput> list = generate(term , template);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		return output;
	}
	
	private List<TermGenerationOutput> generate(String term, TermTemplate template) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = template.getFields().get(0);
		parameters.setTermValues(field.getName(), Arrays.asList(term)); 
	
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		
		return list;
	}
	
	private TermTemplate getPlantDevelopmentTemplate() {
		List<TermTemplate> availableTemplates = generationEngine.getAvailableTemplates();
		return availableTemplates.get(0);
	}
	
	private TermTemplate getPlantFormationTemplate() {
		return generationEngine.getAvailableTemplates().get(1);
	}
	
	private TermTemplate getPlantMaturationTemplate() {
		return generationEngine.getAvailableTemplates().get(2);
	}
	
	private TermTemplate getPlantMorphogenesisTemplate() {
		return generationEngine.getAvailableTemplates().get(3);
	}
	
	private TermTemplate getPlantStructuralOrganizationTemplate() {
		return generationEngine.getAvailableTemplates().get(4);
	}
	
	private void renderFrame(final Frame frame) throws InvalidManagedInstanceException  {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
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
}
