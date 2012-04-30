package org.bbop.termgenie.rules.chebi;

import static org.junit.Assert.*;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.rules.chebi.ChemicalTermGenieScriptCatalogXmlTestRunner.ChemicalTestOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.OWLClassExpression;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class ChemicalManchesterSyntaxToolTest {

	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new ChemicalTestOntologyModule());
		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Test
	public void testManchesterSyntaxTool() {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("ChemicalImporter");
		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		OntologyTask task = new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				ManchesterSyntaxTool tool = new ManchesterSyntaxTool(managed.getSourceOntology(), managed.getSupportOntologySet());
				
				assertNotNull(managed.getOWLObjectByLabel("has participant"));
				
				assertNotNull(tool.parseManchesterExpression("UCHEBI_30769"));
				assertNotNull(tool.parseManchesterExpression("GO_0008152"));
				
				OWLClassExpression expression = tool.parseManchesterExpression("GO_0008152 and 'has participant' some UCHEBI_30769");
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
