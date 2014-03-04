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
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.rules.OldTestOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class ExportTest {

	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_chemical.xml", false, true, null),
				OldTestOntologyModule.chemical(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	

	@Test
	public void test_export() throws Exception {
		TermTemplate termTemplate = getExportTemplate();
		String id = "CHEBI:4534"; // difenoxin // this is a chemical synthesized compound, probably never used in GO
		List<TermGenerationInput> generationTasks = createResponseToTask(termTemplate, id);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		
		TermGenerationOutput output1 = list.get(0);
		assertNull(output1.getError());
		Frame term1 = output1.getTerm();
		renderFrame(term1);
		assertEquals("difenoxin export", term1.getTagValue(OboFormatTag.TAG_NAME));
		
	}
	
	@Test
	public void test_equivalent_existing() throws Exception {
		TermTemplate termTemplate = getExportTemplate();
		String id = "CHEBI:32456"; // cysteinate(1-)
		
		List<TermGenerationInput> generationTasks = createResponseToTask(termTemplate, id);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertEquals("Failed to create the term cysteinate(1-) export with the logical definition: \"GO_0006810 and 'exports' some CHEBI_32456\" " +
				"The term GO:0033228 'cysteine export' with the same logic definition already exists", output1.getError());
		
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
	
	private TermTemplate getExportTemplate() {
		return generationEngine.getAvailableTemplates().get(9);
	}

	private List<TermGenerationInput> createResponseToTask(TermTemplate termTemplate, final String term) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		TemplateField field = termTemplate.getFields().get(0);
		parameters.setTermValues(field.getName(), Arrays.asList(term)); 
	
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		return generationTasks;
	}

}
