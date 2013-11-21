package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;


public class ReloadOntologyTest {

	private static OntologyTaskManager manager = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		IRIMapper iriMapper = new CatalogXmlIRIMapper(null, "src/test/resources/ontologies/import-test-case/catalog-v001.xml");
		OntologyConfiguration configuration = new XMLOntologyConfiguration("ontology-configuration_import-test-case.xml", false);
		ReloadingOntologyLoader loader = new ReloadingOntologyLoader(configuration , iriMapper, 6L, TimeUnit.HOURS);
		manager = loader.getOntologyManager();
	}

	@Test
	public void test() throws Exception {
		manager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {

			@Override
			public Modified run(OWLGraphWrapper managed)
			{
				OWLObject owlObject = managed.getOWLObjectByIdentifier("CHEBI:24309");
				assertNotNull(owlObject);
				return Modified.no;
			}
			
		});
		Logger.getLogger(ReloadOntologyTest.class).info(" -------- Initialize Reload --------- ");
		manager.updateManaged();
		
		manager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {

			@Override
			public Modified run(OWLGraphWrapper managed)
			{
				OWLObject owlObject = managed.getOWLObjectByIdentifier("CHEBI:24309");
				assertNotNull(owlObject);
				return Modified.no;
			}
			
		});
	}
	
	@Ignore("Long running. Is useful for detecting memory leaks.")
	@Test
	public void testRepeated() throws Exception {
		manager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {

			@Override
			public Modified run(OWLGraphWrapper managed)
			{
				OWLObject owlObject = managed.getOWLObjectByIdentifier("CHEBI:24309");
				assertNotNull(owlObject);
				return Modified.no;
			}
			
		});
		for (int i=0;i<100;i++) {
			Logger.getLogger(ReloadOntologyTest.class).info(" -------- Initialize Reload "+i+"--------- ");
			manager.updateManaged();
			
			manager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {
	
				@Override
				public Modified run(OWLGraphWrapper managed)
				{
					OWLObject owlObject = managed.getOWLObjectByIdentifier("CHEBI:24309");
					assertNotNull(owlObject);
					return Modified.no;
				}
				
			});
		}
	}

}
