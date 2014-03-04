package org.bbop.termgenie.rules.impl;

import static org.junit.Assert.*;

import java.util.Collections;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.rules.OldTestOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class CheckEquivalentClassesAxiomsTest {

	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new OldTestOntologyModule("ontology-configuration_simple.xml"));
		loader = injector.getInstance(OntologyLoader.class);
	}

	@Test
	public void test_check_for_regulation_in_axioms() throws Exception {
		OntologyTaskManager ontologyTaskManager = loader.getOntologyManager();
		ontologyTaskManager.runManagedTask(new OntologyTaskManager.OntologyTask() {

			@Override
			protected void runCatching(final OWLGraphWrapper graph)
			{
				assertFalse(checkGenusAxioms(graph, "GO:0002754")); //intracellular vesicle pattern recognition receptor signaling pathway
				assertTrue(checkGenusAxioms(graph, "GO:0051302")); //regulation of cell division
				assertTrue(checkGenusAxioms(graph, "GO:0051782")); //negative regulation of cell division

			}
			
			private boolean checkGenusAxioms(final OWLGraphWrapper graph, final String id)
			{
				OWLClass checkedFor = graph.getOWLClassByIdentifier("GO:0065007"); // biological regulation
				OWLClass targetClass = graph.getOWLClassByIdentifier(id);
				return RuleHelper.containsClassInEquivalenceAxioms(targetClass, Collections.singleton(checkedFor), graph.getSourceOntology());
			}

		});
	}
	
}
