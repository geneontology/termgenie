package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import owltools.InferenceBuilder;
import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class UpdateRelationShipTest {

	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;
	private static ReasonerFactory reasonerFactory;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule() {

			@Override
			protected void bindOntologyConfiguration() {
				bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple.xml");
			}
		}, new ReasonerModule(null));

		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
		reasonerFactory = injector.getInstance(ReasonerFactory.class);
	}

	@Test
	public void test1() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		ontologyManager.runManagedTask(new OntologyTask() {

			@Override
			protected void runCatching(final OWLGraphWrapper wrapper)
					throws TaskException, Exception
			{
				ReasonerTaskManager reasonerTaskManager = reasonerFactory.getDefaultTaskManager(wrapper);
				reasonerTaskManager.runManagedTask(new UpdateRelationShipTask(wrapper));
			}
		});

	}

	static final class UpdateRelationShipTask implements ManagedTask<OWLReasoner> {

		private final OWLOntology ontology;
		private final OWLGraphWrapper wrapper;

		UpdateRelationShipTask(OWLGraphWrapper wrapper) {
			this.ontology = wrapper.getSourceOntology();
			this.wrapper = wrapper;
		}

		@Override
		public Modified run(OWLReasoner reasoner) {

			buildInferences(reasoner);

			OWLClass rsd = getOWLClassByIdentifier("GO:0072001");
			// OWLClass rtd = getOWLClassByIdentifier("GO:0061333");
			OWLClass ecp = getOWLClassByIdentifier("GO:0050673");
			OWLClass ecprtd = getOWLClassByIdentifier("GO:2001013");

			OWLClass exampleClass = createInvolvedInExample(rsd, ecp, "ExampleLabel");

			reasoner.flush();

			Pair<Collection<OWLAxiom>, Collection<OWLAxiom>> inferences = buildInferences(reasoner);

			System.out.println("Inferred Axioms: " + inferences.getOne().size());
			Collection<OWLAxiom> redundantAxioms = inferences.getTwo();
			System.out.println("Redundant axioms: " + redundantAxioms.size());
			for (OWLAxiom redundant : redundantAxioms) {
				System.out.println(redundant);
			}

			Pair<List<Clause>,Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(exampleClass, wrapper);
			List<Clause> relations = pair.getOne();
			System.out.println("------------");
			System.out.println("Relations for: "+Owl2Obo.getIdentifier(exampleClass.getIRI()));
			for (Clause relation : relations) {
				System.out.println(relation);
			}
			assertEquals(4, relations.size());

			NodeSet<OWLClass> subClasses = reasoner.getSubClasses(exampleClass, true);
			assertTrue(subClasses.containsEntity(ecprtd));
			assertTrue(subClasses.isSingleton());

			Pair<List<Clause>,Set<OWLAxiom>> pair2 = OwlTranslatorTools.extractRelations(ecprtd, wrapper);
			List<Clause> relations2 = pair2.getOne();
			System.out.println("------------");
			System.out.println("Relations for: "+Owl2Obo.getIdentifier(ecprtd.getIRI()));
			assertEquals(4, relations2.size());
			for (Clause relation : relations2) {
				System.out.println(relation);
				if(relation.getValue().equals("GO:0050673")){
					assertFalse(OboFormatTag.TAG_IS_A.getTag().equals(relation.getTag()));
				}
			}

			return Modified.no;
		}

		private Pair<Collection<OWLAxiom>, Collection<OWLAxiom>> buildInferences(OWLReasoner reasoner) {
			assertTrue(reasoner.isConsistent());
			InferenceBuilder inferenceBuilder = new InferenceBuilder(wrapper, (OWLReasonerFactory) null, false);
			inferenceBuilder.setReasoner(reasoner);
		
			List<OWLAxiom> inferences = inferenceBuilder.buildInferences();
			for (OWLAxiom owlAxiom : inferences) {
				AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
				ontology.getOWLOntologyManager().applyChange(addAx);
			}
			Collection<OWLAxiom> redundants = inferenceBuilder.getRedundantAxioms();
			for (OWLAxiom redundant : redundants) {
				RemoveAxiom removeAx = new RemoveAxiom(ontology, redundant);
				ontology.getOWLOntologyManager().applyChange(removeAx);
			}
			return new Pair<Collection<OWLAxiom>, Collection<OWLAxiom>>(inferences, redundants);
		}

		private OWLClass createInvolvedInExample(OWLClass rsd, OWLClass ecp, String label) {
			try {
				OWLOntologyManager manager = ontology.getOWLOntologyManager();
				OWLDataFactory factory = manager.getOWLDataFactory();
				IRI iri = IRI.create(Obo2OWLConstants.DEFAULT_IRI_PREFIX + "GO_TEMP-0001");
				OWLClass owlClass = factory.getOWLClass(iri);
				OWLDeclarationAxiom owlDeclarationAxiom = factory.getOWLDeclarationAxiom(owlClass);
				manager.applyChange(new AddAxiom(ontology, owlDeclarationAxiom));

				OWLAnnotationAssertionAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
						iri,
						factory.getOWLLiteral(label));
				manager.applyChange(new AddAxiom(ontology, labelAxiom));
				
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(ontology);
				OWLClassExpression expression = tool.parseManchesterExpression(tool.getId(ecp) + " and 'part_of' some " + tool.getId(rsd));
				OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(owlClass,
						expression);
				manager.applyChange(new AddAxiom(ontology, axiom));
				return owlClass;
			} catch (OWLOntologyRenameException exception) {
				fail(exception.getMessage());
			} catch (ParserException exception) {
				fail(exception.getMessage());
			}
			return null;
		}

		private OWLClass getOWLClassByIdentifier(String id) {
			return wrapper.getOWLClass(wrapper.getOWLObjectByIdentifier(id));
		}
	}
}
