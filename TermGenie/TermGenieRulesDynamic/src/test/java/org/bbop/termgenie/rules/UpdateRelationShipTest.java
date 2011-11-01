package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.Relation;
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
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
				bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple.xml");
			}
		}, new ReasonerModule("elk", null));

		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
		reasonerFactory = injector.getInstance(ReasonerFactory.class);
	}

	@Test
	public void test1() {
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

			Pair<List<OWLAxiom>, List<OWLAxiom>> inferences = buildInferences(reasoner);

			System.out.println("Inferred Axioms: " + inferences.getOne().size());
			List<OWLAxiom> redundantAxioms = inferences.getTwo();
			System.out.println("Redundant axioms: " + redundantAxioms.size());
			for (OWLAxiom redundant : redundantAxioms) {
				System.out.println(redundant);
			}

			OBODoc oboDoc = createOboDoc();
			List<IRelation> relations = getRelations(exampleClass, oboDoc);
			System.out.println("------------");
			for (IRelation relation : relations) {
				System.out.println(relation);
			}
			assertEquals(4, relations.size());

			NodeSet<OWLClass> subClasses = reasoner.getSubClasses(exampleClass, true);
			assertTrue(subClasses.containsEntity(ecprtd));
			assertTrue(subClasses.isSingleton());

			List<IRelation> relations2 = getRelations(ecprtd, oboDoc);
			System.out.println("------------");
			assertEquals(4, relations2.size());
			for (IRelation relation : relations2) {
				System.out.println(relation);
				if(relation.getTarget().equals("GO:0050673")){
					String type = Relation.getType(relation.getProperties());
					assertFalse(OboFormatTag.TAG_IS_A.getTag().equals(type));
				}
			}

			return Modified.no;
		}

		private Pair<List<OWLAxiom>, List<OWLAxiom>> buildInferences(OWLReasoner reasoner) {
			assertTrue(reasoner.isConsistent());
			InferenceBuilder inferenceBuilder = new InferenceBuilder(wrapper, (OWLReasonerFactory) null, false);
			inferenceBuilder.setReasoner(reasoner);
		
			List<OWLAxiom> inferences = inferenceBuilder.buildInferences();
			for (OWLAxiom owlAxiom : inferences) {
				AddAxiom addAx = new AddAxiom(ontology, owlAxiom);
				ontology.getOWLOntologyManager().applyChange(addAx);
			}
			List<OWLAxiom> redundants = inferenceBuilder.getRedundantAxioms();
			for (OWLAxiom redundant : redundants) {
				RemoveAxiom removeAx = new RemoveAxiom(ontology, redundant);
				ontology.getOWLOntologyManager().applyChange(removeAx);
			}
			return new Pair<List<OWLAxiom>, List<OWLAxiom>>(inferences, redundants);
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
				
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(ontology, Collections.<OWLOntology> emptyList());
				OWLClassExpression expression = tool.parseManchesterExpression(tool.mapOwlObject(ecp) + " and part_of some " + tool.mapOwlObject(rsd));
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

		private OBODoc createOboDoc() {
			try {
				Owl2Obo owl2Obo = new Owl2Obo();
				OBODoc oboDoc = owl2Obo.convert(ontology);
				return oboDoc;
			} catch (OWLOntologyCreationException exception) {
				fail(exception.getMessage());
			}
			return null;
		}

		private List<IRelation> getRelations(OWLClass owlClass, OBODoc oboDoc) {
			String frameId = Owl2Obo.getIdentifier(owlClass.getIRI());
			Frame frame = oboDoc.getTermFrame(frameId);
			List<IRelation> relations = OBOConverterTools.extractRelations(frame, oboDoc);
			return relations;
		}
	}
}
