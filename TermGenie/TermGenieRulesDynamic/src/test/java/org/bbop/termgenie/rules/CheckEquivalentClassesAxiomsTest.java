package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Collections;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class CheckEquivalentClassesAxiomsTest {

	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule() {

			@Override
			protected void bindOntologyConfiguration() {
				bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple.xml");
			}
		});

		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}

	@Test
	public void test_check_for_regulation_in_axioms() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontologyTaskManager = loader.getOntology(ontology);
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
