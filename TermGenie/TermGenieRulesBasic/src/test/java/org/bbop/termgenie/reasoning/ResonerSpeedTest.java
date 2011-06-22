package org.bbop.termgenie.reasoning;

import static org.junit.Assert.*;

import java.util.Map;

import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
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

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import owltools.graph.OWLGraphWrapper;

public class ResonerSpeedTest {

	private static OWLOntology ontology;
	private static OWLGraphWrapper wrapper;

	@BeforeClass
	public static void beforeClass() {
		Map<String, ConfiguredOntology> ontologies = DefaultOntologyConfiguration.getOntologies();
		ConfiguredOntology configuredOntology = ontologies.get("GeneOntology");
		wrapper = DefaultOntologyLoader.getOntology(configuredOntology);
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
	
	private void testReasoner(OWLReasonerFactory reasonerFactory, String name) {
		System.out.println("----------------------");
		System.out.println("Reasoner: "+name);
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		
		Timer t1 = new Timer();
		boolean isConsistent = reasoner.isConsistent();
		t1.stop();
		assertTrue(isConsistent);
		
		OWLClass superDescription = get("GO:0006915"); // apoptosis
		OWLClass subDescriptionIsa = get("GO:0044346");// fibroblast apoptosis
		OWLClass subDescriptionPartOf = get("GO:0070782"); // phosphatidylserine exposure on apoptotic cell surface
		
		Timer t2 = new Timer();
		NodeSet<OWLClass> subClasses = reasoner.getSubClasses(superDescription, false);
		t2.stop();
		
		Timer t3 = new Timer();
		boolean containsIsa = subClasses.containsEntity(subDescriptionIsa);
		boolean containsPartOf = subClasses.containsEntity(subDescriptionPartOf);
		t3.stop();
		
		System.out.println(subClasses);
		System.out.println("descendant isa: "+containsIsa);
		System.out.println("descendant part of: "+containsPartOf);
		
		Timer t4 = new Timer();
		Node<OWLClass> topClassNode = reasoner.getTopClassNode();
		t4.stop();
		
		System.out.println("\n Runtime");
		System.out.println("isConsistent:    "+t1.getTimeString());
		System.out.println("getSubClasses:   "+t2.getTimeString());
		System.out.println("containsEntity:  "+t3.getTimeString());
		System.out.println("getTopClassNode: "+t4.getTimeString());
		System.out.println("----------------------");
		
		assertTrue(containsIsa);
		assertFalse(containsPartOf);
		assertTrue(topClassNode.getSize() > 0);
	}
	
	private OWLClass get(String id) {
		OWLObject x = wrapper.getOWLObjectByIdentifier(id);
		if (x != null) {
			return wrapper.getOWLClass(x);
		}
		return null;
	}
	
	private static class Timer {
		
		private final long start;
		private long stop = 0;
		
		public Timer() {
			start = System.currentTimeMillis();
		}
		
		public void stop() {
			stop = System.currentTimeMillis();
		}
		
		public String getTimeString() {
			long ellapsed = stop - start;
			String ms = Long.toString(ellapsed % 1000L);
			while (ms.length() < 3) {
				ms = "0" + ms;
			}
			long s = ellapsed / 1000L;
			return Long.toString(s)+"."+ms+" s";
		}
	}
}
