package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.LocalFileIRIMapperTest.TestLocalFileIRIMapper;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.graph.OWLGraphWrapper;


public class OWLImportTest {

	private static File testFolder = null;
	
	@BeforeClass
	public static void beforeClass() {
		testFolder = TempTestFolderTools.createTestFolder(LocalFileIRIMapperTest.class);
	}
	
	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}
	
	@Test
	public void testImport() {
		List<String> converted = new ArrayList<String>();
		LocalFileIRIMapper mapper = new TestLocalFileIRIMapper(testFolder, converted);
		OntologyConfiguration c = new DefaultOntologyConfiguration("owlimport-test-ontology-configuration.settings", false);
		OntologyLoader loader = new DefaultOntologyLoader(c, mapper, null, null);
		List<OntologyTaskManager> ontologies = loader.getOntologies();
		assertEquals(1, ontologies.size());
		assertEquals(1, converted.size());
		OntologyTaskManager manager = ontologies.get(0);
		manager.runManagedTask(new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper graph) {
				OWLClass owlClass = graph.getOWLClassByIdentifier("GO:0019012");
				assertNotNull(owlClass);
				boolean found = false;
				for(OWLOntology ontology : graph.getAllOntologies()) {
					Set<OWLDisjointClassesAxiom> axioms = ontology.getDisjointClassesAxioms(owlClass);
					if (!axioms.isEmpty()) {
						found = true;
						assertEquals(5, axioms.size());
					}
				}
				assertTrue(found);
			}
		});
	}
}
