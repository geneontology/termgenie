package org.bbop.termgenie.rules.mp;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
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
import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class MPOntologyTest {

	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_mp.xml", true, true, null),
				new OntologyModule("ontology-configuration_mp_test.xml"),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Test
	@Ignore("Deactivated, reasoning seems to work now.")
	public void writeOntologyFile() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper graph) throws TaskException, Exception {
				OWLOntology source = graph.getSourceOntology();
				OWLOntologyManager m = graph.getManager();
				FileOutputStream outputStream = null;
				try {
					outputStream = new FileOutputStream("mp-test.owl");
					m.saveOntology(source, outputStream);
				}
				finally {
					IOUtils.closeQuietly(outputStream);
				}
			}
		};
		ontologyManager.runManagedTask(task);
	}
	
	@Test
	public void testReasoning() throws Exception {
		final String expr = "('has part' some (PATO_0000694 and 'inheres in' some GO_0001570 and 'has modifier' some PATO_0000460))";
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(final OWLGraphWrapper graph) throws TaskException, Exception {
				OWLOntologyManager manager = graph.getManager();
				OWLDataFactory factory = graph.getDataFactory();
				final OWLClass owlThing = factory.getOWLThing();
				OWLOntology target = graph.getSourceOntology();
				Set<OWLAxiom> added = new HashSet<OWLAxiom>();
				OWLReasoner reasoner = null;
				OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
				try {
					IRI clsIRI = IRI.generateDocumentIRI();
					OWLClass cls = factory.getOWLClass(clsIRI);
					added.add(factory.getOWLDeclarationAxiom(cls));
					
					ManchesterSyntaxTool tool = new ManchesterSyntaxTool(target, null);
					OWLClassExpression expression = tool.parseManchesterExpression(expr);
					added.add(factory.getOWLEquivalentClassesAxiom(cls, expression));
					
					manager.addAxioms(target, added);
					
					reasoner = reasonerFactory.createReasoner(target);
					
					final Set<OWLClass> equiv = reasoner.getEquivalentClasses(cls).getEntitiesMinus(cls);
					final Set<OWLClass> direct = reasoner.getSuperClasses(cls, true).getFlattened();
					direct.remove(owlThing);
					final Set<OWLClass> indirect = reasoner.getSuperClasses(cls, false).getFlattened();
					indirect.remove(owlThing);
					assertTrue(equiv.isEmpty());
					assertTrue(direct.size() > 0);
					assertTrue(indirect.size() > direct.size());
					
				}
				finally {
					if (reasoner != null) {
						reasoner.dispose();
					}
					if (!added.isEmpty()) {
						manager.removeAxioms(target, added);
					}
				}
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
	}
	
	
	@Test
	public void testSyntax() throws Exception {
		final String expr = "'has part' some (PATO_0000001 and 'inheres in' some (CHEBI_17234 and 'part of' some UBERON_3010346) and 'has component' some PATO_0000460)";
//		final String expr = "('has part' some (PATO_0000051 and 'inheres in' some UBERON_0002028 and 'has component' some PATO_0000460))";
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), null);
				OWLClassExpression expression = tool.parseManchesterExpression(expr);
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
	public void test_abnormal_morphology() throws Exception {
//		String id = "UBERON:0002028"; // hindbrain, exists already
		String id = "GO:0005791"; // rough endoplasmic reticulum
		generateSingle(getTemplate("abnormal_morphology"), id);
		
	}
	
	@Test
	public void test_abnormal_process() throws Exception {
		// String id = "GO:0001783"; // B cell apoptosis, exists already
		// String id = "GO:0004031"; // aldehyde oxidase activity, exists already MP:0020026
		// String id = "GO:0018488"; // aryl-aldehyde oxidase activity
		String id = "GO:0002516"; // B cell deletion
		// String id = "GO:0044691"; // tooth eruption, exists already MP:0000119
		generateSingle(getTemplate("abnormal_process"), id);
	}
	
	@Test
	public void test_abnormal_process_in_location() throws Exception {
		String process = "GO:0002516"; // B cell deletion
		String location = "UBERON:0002106"; // spleen
		generateSingleTwoFields(getTemplate("abnormal_process_in_location"), process, location);
	}
	
	@Test
	public void test_early_onset_process() throws Exception {
		String id = "GO:0061648"; // tooth replacement
		generateSingle(getTemplate("late_early_onset_process"), id, "early");
	}
	
	@Test
	public void test_delayed_onset_process() throws Exception {
		String id = "GO:0061648"; // tooth replacement
		generateSingle(getTemplate("late_early_onset_process"), id, "delayed");
	}
	
	@Test
	public void test_abnormal_onset_process() throws Exception {
		String process = "GO:0044691"; // tooth eruption
		String onset = "PATO:0001484"; // recent
		generateSingleTwoFields(getTemplate("abnormal_onset_process"), onset, process);
	}
	
	@Test
	public void test_abnormal_level() throws Exception {
		String chemical = "CHEBI:17234"; // glucose
		String location = "UBERON:0002106"; // spleen
		List<TermGenerationOutput> terms = generate(getTemplate("abnormal_levels"), Arrays.asList(chemical, location), "unspecified", "greater", "reduced");
		assertNotNull(terms);
		assertEquals(terms.get(0).getError(), 3, terms.size());
		for (TermGenerationOutput output : terms) {
			assertNull(output.getError(), output.getError());
			Set<OWLAxiom> axioms = output.getOwlAxioms();
			int subClassCount = 0;
			for (OWLAxiom axiom : axioms) {
				if (axiom instanceof OWLSubClassOfAxiom) {
					subClassCount+= 1;
				}
			}
			render(output);
			assertTrue(subClassCount > 0);
		}
	}
	
	private void generateSingle(TermTemplate template, String id, String...prefixes) {
		List<TermGenerationOutput> list = generate(template, Arrays.asList(id), prefixes);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		Set<OWLAxiom> axioms = output.getOwlAxioms();
		int subClassCount = 0;
		for (OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				subClassCount+= 1;
			}
		}
		render(output);
		assertTrue(subClassCount > 0);
	}
	
	private void generateSingleTwoFields(TermTemplate template, String...fields) {
		List<TermGenerationOutput> list = generate(template, Arrays.asList(fields));
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError(), output.getError());
		Set<OWLAxiom> axioms = output.getOwlAxioms();
		int subClassCount = 0;
		for (OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				subClassCount+= 1;
			}
		}
		render(output);
		assertTrue(subClassCount > 0);
	}
	
	private List<TermGenerationOutput> generate(TermTemplate template, List<String> values, String...prefixes) {
		TermGenerationParameters parameters = new TermGenerationParameters();
		List<TemplateField> fields = template.getFields();
		for(int i=0;i<fields.size();i++) {
			if (values != null && values.size() > i) {
				TemplateField field = fields.get(i);
				parameters.setTermValues(field.getName(), Arrays.asList(values.get(i))); 
				if (i==0 && prefixes != null && prefixes.length > 0) {
					parameters.setStringValues(field.getName(), Arrays.asList(prefixes));
				}	
			}
		}
		
		TermGenerationInput input = new TermGenerationInput(template, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);
		
		return list;
	}
	
	private TermTemplate getTemplate(String name) {
		List<TermTemplate> templates = generationEngine.getAvailableTemplates();
		for (TermTemplate template : templates) {
			String currentName = template.getName();
			if (name.equals(currentName)) {
				return template;
			}
		}
		throw new RuntimeException("No template found with name: "+name);
	}
	
	private void render(TermGenerationOutput output) {
		System.out.println("-----------");
		Set<OWLAxiom> axioms = output.getOwlAxioms();
		for (OWLAxiom owlAxiom : axioms) {
			System.out.println(owlAxiom);
		}
		final Frame frame = output.getTerm();
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
		try {
			ontologyManager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		}
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
	}
}
