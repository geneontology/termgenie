package org.bbop.termgenie.ontology;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.OntologyIdStore.OntologyIdStoreException;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OntologyIdStoreTest {
	
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
	
	
	protected EntityManagerFactory createDerby() {
		Map<String, String> properties = new HashMap<String, String>();
		File dbFolder = new File(testFolder,"derby");
		dbFolder.mkdir();
		System.setProperty("derby.system.home", dbFolder.getAbsolutePath());
		properties.put("openjpa.ConnectionURL", "jdbc:derby:directory:OntologyIDStore;create=true");
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("derby", properties);
		assertNotNull(emf);
		return emf;
	}
	
	protected EntityManagerFactory createHsqlDBFile() {
		Map<String, String> properties = new HashMap<String, String>();
		File dbFolder = new File(testFolder,"hslqdb");
		dbFolder.mkdir();
		String connectionURL = "jdbc:hsqldb:file:"+dbFolder.getAbsolutePath()+"/OntologyIDStore";
		properties.put("openjpa.ConnectionURL", connectionURL);
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hslqdb", properties);
		assertNotNull(emf);
		return emf;
	}
	
	protected EntityManagerFactory createSqlite() {
		Map<String, String> properties = new HashMap<String, String>();
		File dbFolder = new File(testFolder,"sqlite");
		dbFolder.mkdir();
		String connectionURL = "jdbc:sqlite:"+dbFolder.getAbsolutePath()+"/OntologyIDStore";
		properties.put("openjpa.ConnectionURL", connectionURL);
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("sqlite", properties);
		assertNotNull(emf);
		return emf;
	}
	
	
	@Test
	public void testOntologyIdStoreHsqlDB() {
		testOntologyIdStore(createHsqlDBFile());
	}
	
	@Test
	public void testOntologyIdStoreSqlite() {
		testOntologyIdStore(createSqlite());
	}
	
	@Test
	public void testOntologyIdStoreDerby() {
		testOntologyIdStore(createDerby());
	}
	
	@SuppressWarnings("null")
	private void testOntologyIdStore(EntityManagerFactory emf) {
		EntityManager entityManager = emf.createEntityManager();
		assertNotNull(entityManager);
		
		String storeConfig = "testOntologyName \t foo:000000 \t 41 \t 48 \n"+
							 "barOntology \t bar:000000 \t 9000 \t 10000";
		
		OntologyIdStore store = new OntologyIdStore(new StringInputStream(storeConfig), entityManager);
		
		Ontology ontology = new Ontology("testOntologyName", null, null) { /* intentionally empty */};
		
		assertEquals("foo:000041", store.getNewId(ontology, entityManager));
		assertEquals("foo:000042", store.getNewId(ontology, entityManager));
		assertEquals("foo:000043", store.getNewId(ontology, entityManager));
		
		entityManager.close();
		entityManager = null;
		
		entityManager = emf.createEntityManager();
		assertNotNull(entityManager);
		
		store = new OntologyIdStore(new StringInputStream(storeConfig), entityManager);
		assertEquals("foo:000044", store.getNewId(ontology, entityManager));
		assertEquals("foo:000045", store.getNewId(ontology, entityManager));
		assertEquals("foo:000046", store.getNewId(ontology, entityManager));
		
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
