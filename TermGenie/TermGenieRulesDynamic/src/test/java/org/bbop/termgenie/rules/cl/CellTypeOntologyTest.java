package org.bbop.termgenie.rules.cl;

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
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
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


public class CellTypeOntologyTest {

	private static TermGenerationEngine generationEngine;
	private static ConfiguredOntology go;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void beforeClass() {
		List<String> ignoreMappings = Arrays.asList("http://purl.obolibrary.org/obo/go.owl", "http://purl.obolibrary.org/obo/go/extensions/x-chemical.owl");
		
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_cl.xml", false, null),
				new XMLReloadingOntologyModule("ontology-configuration_cl.xml", ignoreMappings, null),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
		go = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("GeneOntology");
	}
	
	
	@Test
	public void testPlantDevelopment() throws Exception {
		String term = "CL:0011003"; // magnocellular neurosecretory cell
		TermGenerationOutput output = generateSingle(term , getCellApotosisTemplate());
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
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(go, generationTasks, false, null);
		
		return list;
	}
	
	private TermTemplate getCellApotosisTemplate() {
		List<TermTemplate> availableTemplates = generationEngine.getAvailableTemplates();
		return availableTemplates.get(0);
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
}
