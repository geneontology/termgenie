package org.bbop.termgenie.ontology;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.tools.ant.filters.StringInputStream;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.OntologyIdStore.OntologyIdStoreException;
import org.bbop.termgenie.presistence.EntityManagerFactoryProvider;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OntologyIdStoreTest {
	
	private static EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();
	private static File testFolder;
	
	@BeforeClass
	public static void beforeClass() {
		testFolder = TempTestFolderTools.createTestFolder(OntologyIdStoreTest.class);
	}
	
	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Before
	public void before() throws IOException {
		// clear test folder for each test
		FileUtils.cleanDirectory(testFolder);
	}
	
	@Test
	public void testOntologyIdStoreHsqlDB() {
		testOntologyIdStore(EntityManagerFactoryProvider.HSQLDB);
	}
	
	@Test
	public void testOntologyIdStoreSqlite() {
		testOntologyIdStore(EntityManagerFactoryProvider.SQLITE);
	}
	
	@Test
	public void testOntologyIdStoreDerby() {
		testOntologyIdStore(EntityManagerFactoryProvider.DERBY);
	}
	
	@Test
	public void testOntologyIdStoreH2() {
		testOntologyIdStore(EntityManagerFactoryProvider.H2);
	}
	
	@SuppressWarnings("null")
	private void testOntologyIdStore(String label) {
		EntityManagerFactory emf = provider.createFactory(testFolder, label, "OntologyIdStore");
		assertNotNull(emf);
		EntityManager entityManager = emf.createEntityManager();
		assertNotNull(entityManager);
		
		String storeConfig = "testOntologyName \t foo:000000 \t 41 \t 48 \n"+
							 "barOntology \t bar:000000 \t 9000 \t 10000";
		
		StopWatch watch1 = new StopWatch();
		watch1.start();
		OntologyIdStore store = new OntologyIdStore(new StringInputStream(storeConfig), entityManager);
		watch1.stop();
		
		Ontology ontology = new Ontology("testOntologyName", null, null) { /* intentionally empty */};
		
		StopWatch watch2 = new StopWatch();
		watch2.start();
		assertEquals("foo:000041", store.getNewId(ontology, entityManager));
		assertEquals("foo:000042", store.getNewId(ontology, entityManager));
		assertEquals("foo:000043", store.getNewId(ontology, entityManager));
		watch2.stop();
		
		entityManager.close();
		entityManager = null;
		
		entityManager = emf.createEntityManager();
		assertNotNull(entityManager);
		
		StopWatch watch3 = new StopWatch();
		watch3.start();
		store = new OntologyIdStore(new StringInputStream(storeConfig), entityManager);
		assertEquals("foo:000044", store.getNewId(ontology, entityManager));
		assertEquals("foo:000045", store.getNewId(ontology, entityManager));
		assertEquals("foo:000046", store.getNewId(ontology, entityManager));
		watch3.stop();
		
		System.out.println(label+" Loading :"+watch1);
		System.out.println(label+" Read and Write1 :"+watch2);
		System.out.println(label+" Read and Write2 :"+watch3);
		
		OntologyIdStoreException expected = null;
		try {
			store.getNewId(ontology, entityManager);
		} catch (OntologyIdStoreException exception) {
			expected = exception;
		}
		assertNotNull(expected);
		assertTrue(expected.getMessage().contains("Upper limit"));
		assertTrue(expected.getMessage().contains("of the ID range reached for ontology"));
	}

}
