package org.bbop.termgenie.ontology;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.bbop.termgenie.ontology.entities.OntologyIdInfo;
import org.junit.Test;


public class PlainOntologyIdStoreConfigurationTest {

	@Test
	public void testGetInfos() {
		String longPattern = "long:00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		
		String storeConfig = "testOntologyName \t foo:000000 \t 41 \t 48 \n" + //
		"barOntology \t bar:000000 \t 9000 \t 10000 \n" + //
		"longOntology \t "+longPattern+" \t 9000 \t 10000";

		InputStream inputStream = new ByteArrayInputStream(storeConfig.getBytes());
		OntologyIdStoreConfiguration config = new PlainOntologyIdStoreConfiguration(inputStream);
		Map<String, OntologyIdInfo> infos = config.getInfos();
		assertEquals(3, infos.size());
		assertInfo(infos.get("testOntologyName"), "testOntologyName", "foo:000000", 41, 48);
		assertInfo(infos.get("barOntology"), "barOntology", "bar:000000", 9000, 10000);
		assertInfo(infos.get("longOntology"), "longOntology", longPattern, 9000, 10000);
		
	}

	private void assertInfo(OntologyIdInfo info, String name, String pattern, int start, int end) {
		assertNotNull("no pattern for ontology "+name, info);
		assertNotNull(name);
		assertEquals(name, info.getOntologyName());
		assertEquals(pattern, info.getPattern());
		assertEquals(start, info.getCurrent());
		assertEquals(end, info.getMaximum());
	}
}
