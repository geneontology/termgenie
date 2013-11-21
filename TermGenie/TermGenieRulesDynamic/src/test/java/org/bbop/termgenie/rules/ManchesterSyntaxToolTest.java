package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class ManchesterSyntaxToolTest {

	private static OntologyLoader loader;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(createOntologyModule());
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	private static IOCModule createOntologyModule() {
		return new TestDefaultOntologyModule() {

			@Override
			protected void bindOntologyConfiguration() {
				bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple.xml");
			}
		};
	}

	@Test
	public void testManchesterSyntaxTool() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology());
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0019660 and 'occurs in' some GO_0005777");
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
	public void testManchesterSyntaxTool2() throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), null);
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0046836 and 'part_of' some GO_0051402");
				assertNotNull(expression);
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			String message  = task.getMessage() != null ? task.getMessage() : task.getException().getMessage();
			fail(message);	
		}
		
	}

}
