package org.bbop.termgenie.reasoning;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.inject.Injector;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerProcessor;

public class GeneOntologyResonerSpeedTest {

	private static OWLOntology ontology;
	private static OWLGraphWrapper wrapper;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new DefaultOntologyModule());
		OntologyConfiguration config = injector.getInstance(OntologyConfiguration.class);
		Map<String, ConfiguredOntology> ontologies = config.getOntologyConfigurations();
		ConfiguredOntology configuredOntology = ontologies.get("GeneOntology");
		OntologyLoader loader = injector.getInstance(OntologyLoader.class);
		OntologyTaskManager ontologyTaskManager = loader.getOntology(configuredOntology);
		ontologyTaskManager.runManagedTask(new OntologyTaskManager.OntologyTask() {

			@Override
			public Modified run(OWLGraphWrapper managed) {
				wrapper = managed;
				return Modified.no;
			}
		});
		ontology = wrapper.getSourceOntology();
	}

	@Test
	public void testHermit() {
		testReasoner(new Reasoner.ReasonerFactory(), "Hermit");
	}

	@Test
	public void testPellet() {
		testReasoner(PelletReasonerFactory.getInstance(), "Pellet");
	}

	@Test
	public void testJCel() {
		ReasonerTaskManager manager = new ReasonerTaskManager("TestReasonerManager-JCEL") {

			@Override
			protected OWLReasoner updateManaged(OWLReasoner reasoner) {
				// Do nothing
				return reasoner;
			}

			@Override
			protected OWLReasoner createManaged() {
				System.out.println("----------------------");
				System.out.println("Reasoner:  JCEL");
				StopWatch t0 = new StopWatch();
				t0.start();
				JcelReasoner reasoner = new JcelReasoner(ontology);
				JcelReasonerProcessor processor = reasoner.getProcessor();
				processor.precomputeInferences();
				t0.stop();
				System.out.println("precomputeInferences:    " + t0);
				return reasoner;
			}
		};
		testReasoner(manager);
	}

	private void testReasoner(final OWLReasonerFactory reasonerFactory, final String name) {
		ReasonerTaskManager manager = new ReasonerTaskManager("TestReasonerManager-" + reasonerFactory.getReasonerName())
		{

			@Override
			protected OWLReasoner updateManaged(OWLReasoner reasoner) {
				// Do nothing
				return reasoner;
			}

			@Override
			protected OWLReasoner createManaged() {
				System.out.println("----------------------");
				System.out.println("Reasoner: " + name);
				return reasonerFactory.createNonBufferingReasoner(ontology);
			}
		};
		testReasoner(manager);
	}

	private void testReasoner(ReasonerTaskManager manager) {

		manager.runManagedTask(new ReasonerTaskManager.ReasonerTask() {

			@Override
			public Modified run(OWLReasoner reasoner) {
				StopWatch t1 = new StopWatch();
				t1.start();
				boolean isConsistent = reasoner.isConsistent();
				t1.stop();
				assertTrue(isConsistent);

				OWLClass superDescription = get("GO:0006915"); // apoptosis
				OWLClass subDescriptionIsa = get("GO:0044346");// fibroblast
																// apoptosis
				OWLClass subDescriptionPartOf = get("GO:0070782"); // phosphatidylserine
																	// exposure
																	// on
																	// apoptotic
																	// cell
																	// surface

				StopWatch t2 = new StopWatch();
				t2.start();
				NodeSet<OWLClass> subClasses = reasoner.getSubClasses(superDescription, false);
				t2.stop();

				StopWatch t3 = new StopWatch();
				t3.start();
				boolean containsIsa = subClasses.containsEntity(subDescriptionIsa);
				boolean containsPartOf = subClasses.containsEntity(subDescriptionPartOf);
				t3.stop();

				System.out.println(subClasses);
				System.out.println("descendant isa: " + containsIsa);
				System.out.println("descendant part of: " + containsPartOf);

				StopWatch t4 = new StopWatch();
				t4.start();
				Node<OWLClass> topClassNode = reasoner.getTopClassNode();
				t4.stop();

				System.out.println("\n Runtime");
				System.out.println("isConsistent:    " + t1);
				System.out.println("getSubClasses:   " + t2);
				System.out.println("containsEntity:  " + t3);
				System.out.println("getTopClassNode: " + t4);
				System.out.println("----------------------");

				assertTrue(containsIsa);
				assertFalse(containsPartOf);
				assertTrue(topClassNode.getSize() > 0);
				return Modified.no;
			}
		});

	}

	private OWLClass get(String id) {
		OWLObject x = wrapper.getOWLObjectByIdentifier(id);
		if (x != null) {
			if(wrapper.getLabel(x) != null) {
				return wrapper.getOWLClass(x);
			}
		}
		return null;
	}
}
