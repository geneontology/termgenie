package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

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
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class ELKReasonerTest {

	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;
	private static ReasonerFactory reasonerFactory;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new DefaultOntologyModule() {

			@Override
			protected void bindOntologyConfiguration() {
				bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple.xml");
			}
		}, new ReasonerModule("elk"));

		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
		reasonerFactory = injector.getInstance(ReasonerFactory.class);
	}

	@Test
	public void test1() {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		ontologyManager.runManagedTask(new OntologyTask(){

			@Override
			protected void runCatching(final OWLGraphWrapper wrapper) throws TaskException, Exception {
				ReasonerTaskManager reasonerTaskManager = reasonerFactory.getDefaultTaskManager(wrapper);
				reasonerTaskManager.runManagedTask(new ManagedTask<OWLReasoner>() {

					@Override
					public Modified run(OWLReasoner reasoner)
					{
						assertTrue(reasoner.isConsistent());
						OWLObject x = wrapper.getOWLObjectByIdentifier("GO:0006915");
						try {
							NodeSet<OWLClass> classes = reasoner.getSubClasses((OWLClassExpression) x, false);
							assertFalse(classes.isEmpty());
							Set<OWLClass> set = classes.getFlattened();
							assertTrue(set.size() >= 48);
						} catch (Throwable exception) {
							exception.printStackTrace();
							fail(exception.getMessage());
						} 
						return Modified.no;
					}
				});
			}
		});

	}
}
