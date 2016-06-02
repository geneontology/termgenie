package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
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
		Injector injector = TermGenieGuice.createInjector(new OldTestOntologyModule());
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Test
	public void testManchesterSyntaxTool1() throws Exception {
		runExpression("GO_0019660 and 'occurs in' some GO_0005777");
	}
	
	@Test
	public void testManchesterSyntaxTool2() throws Exception {
		runExpression("GO_0046836 and 'part of' some GO_0051402");
	}
	
	@Test
	public void testManchesterSyntaxTool5() throws Exception {
		runExpression("GO_0046836 and BFO_0000050 some GO_0051402");
	}
	
	private void runExpression(final String expressionString) throws Exception {
		OntologyTaskManager ontologyManager = loader.getOntologyManager();
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), null);
				OWLClassExpression expression = tool.parseManchesterExpression(expressionString);
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
