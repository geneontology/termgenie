package org.bbop.termgenie.reasoning;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ResonerFunctionTest {

	private static OWLGraphWrapper go;
	private static OWLGraphWrapper pro;
	private static OWLGraphWrapper uberon;
	private static OWLGraphWrapper plant;
	private static Map<String, ConfiguredOntology> ontologies;
	private static OntologyLoader loader;
	private static ReasonerFactory factory;
	
	@BeforeClass
	public static void beforeClass() {
		Injector injector = Guice.createInjector(new DefaultOntologyModule(), new ReasonerModule());
		OntologyConfiguration config = injector.getInstance(OntologyConfiguration.class);
		ontologies = config.getOntologyConfigurations();
		loader = injector.getInstance(OntologyLoader.class);
		go = load("GeneOntology");
		pro = load("ProteinOntology");
		uberon = load("Uberon");
		plant = load("PO");
		factory = injector.getInstance(ReasonerFactory.class);
	}
	
	private static OWLGraphWrapper load(String name) {
		ConfiguredOntology configuredOntology = ontologies.get(name);
		OntologyTaskManager manager = loader.getOntology(configuredOntology);
		OntologyTaskImplementation task = new OntologyTaskImplementation();
		manager.runManagedTask(task);
		return task.wrapper;
	}
	
	private static final class OntologyTaskImplementation implements OntologyTaskManager.OntologyTask {
		OWLGraphWrapper wrapper = null;
		
		@Override
		public boolean run(OWLGraphWrapper managed) {
			wrapper = managed;
			return false;
		}
	}
	
	@Test
	public void testGO() {
		testDefaultReasonerWithOntology(go, "GO:0006915", "GO:0097049"); 
		//apotosis, motor neuron apoptosis
	}

	@Test
	public void testPlant() {
		testDefaultReasonerWithOntology(plant, "PO:0025017", "PO:0000032");
		// spore
		// tetrad of microspores
	}
	
	@Test(timeout=240*1000L) // 4 minutes timeout!
	public void testPro() {
		testDefaultReasonerWithOntology(pro, "PR:000000024", "PR:000000307");
		// rho-associated protein kinase
		// rho-associated protein kinase 1 isoform
	}
	
	@Test(timeout=240*1000L) // 4 minutes timeout!
	public void testUberon() {
		testDefaultReasonerWithOntology(uberon, "UBERON:0000465", "UBERON:0006108");
		// material anatomical entity
		// corticomedial nuclear complex
	}
	
	private void testDefaultReasonerWithOntology(final OWLGraphWrapper wrapper, final String parent, final String child) {
		ReasonerTaskManager reasonerManager = factory.getDefaultTaskManager(wrapper);
		
		final OWLClass p = wrapper.getOWLClass(wrapper.getOWLObjectByIdentifier(parent));
		assertNotNull(p);
		final OWLClass c = wrapper.getOWLClass(wrapper.getOWLObjectByIdentifier(child));
		assertNotNull(c);
		
		final StopWatch startup = new StopWatch();
		startup.start();
		ManagedTask<OWLReasoner> task = new ManagedTask<OWLReasoner>() {

			@Override
			public boolean run(OWLReasoner reasoner) {
				startup.stop();
				System.out.println("finished preparing reasoner, time: "+startup);
				System.out.println("Reasoning for: "+wrapper.getOntologyId());
				System.out.println("Start quering for super classes of: "+child);
				
				StopWatch t1 = new StopWatch();
				t1.start();
				NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(c, false);
				System.out.println("Finished query. Start checking whether parent is contained: "+parent);
				boolean containsParent = superClasses.containsEntity(p);
				t1.stop();
				System.out.println("Finished contains, time "+t1);
				
				System.out.println("Start quering for sub classes of: "+parent);
				
				StopWatch t2 = new StopWatch();
				t2.start();
				NodeSet<OWLClass> subClasses = reasoner.getSubClasses(p, false);
				System.out.println("Finished query. Start checking whether child is contained:" + child);
				boolean containsChild = subClasses.containsEntity(c);
				t2.stop();
				System.out.println("Finished contains, time "+t2);
				
				assertTrue(containsParent);
				assertTrue(containsChild);
				return false;
			}
		};
		reasonerManager.runManagedTask(task);
	}
}
