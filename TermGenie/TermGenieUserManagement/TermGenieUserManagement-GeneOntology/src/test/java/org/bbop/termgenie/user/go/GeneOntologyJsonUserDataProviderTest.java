package org.bbop.termgenie.user.go;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.user.OrcidUserData;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.XrefUserData;
import org.junit.Test;


public class GeneOntologyJsonUserDataProviderTest {

	@Test
	public void testParse() {
		List<UserData> list = GeneOntologyJsonUserDataProvider.loadUserData("GO.user_data.json");
		
		assertNotNull(list);
		assertEquals(2, list.size());
		
		for (int i = 0; i < list.size(); i++) {
			UserData userData = list.get(i);
			assertNotNull(userData.getGuid());
			assertNotNull(userData.getEmail());
			assertNotNull(userData.getScreenname());
			assertNotNull(userData.getScmAlias());
		}
	}
	
	@Test
	public void testRetrieveByEmail() {
		GeneOntologyJsonUserDataProvider p = new GeneOntologyJsonUserDataProvider("GO.user_data.json");
		UserData data = p.getUserDataPerEMail("testemail@test.com");
		assertNotNull(data);
		assertEquals("testemail@test.com", data.getEmail());
		assertEquals("testemail@test.com", data.getGuid());
		assertNull(data.getOrcid());
	}
	
	@Test
	public void testRetrieveByGuid() {
		GeneOntologyJsonUserDataProvider p = new GeneOntologyJsonUserDataProvider("GO.user_data.json");
		UserData data = p.getUserDataPerGuid("testemail2@test.com", Collections.singletonList("testemail2@test.com"));
		assertNotNull(data);
		assertEquals("testemail2@test.com", data.getEmail());
		assertEquals("testemail2@test.com", data.getGuid());
		assertEquals("Test Name 2", data.getScreenname());
		assertNull(data.getXref());
		assertEquals("testemail2", data.getScmAlias());
		assertEquals("http://orcid.org/0000-0000-0000-0001", data.getOrcid());
	}
	
	@Test
	public void testXrefs() {
		GeneOntologyJsonUserDataProvider p = new GeneOntologyJsonUserDataProvider("GO.user_data.json");
		List<XrefUserData> xrefUserData = p.getXrefUserData();
		assertEquals(1, xrefUserData.size());
		XrefUserData data = xrefUserData.get(0);
		assertEquals("Test Name 1", data.getScreenname());
		assertEquals("GOC:test", data.getXref());
	}
	
	@Test
	public void testOrcids() {
		GeneOntologyJsonUserDataProvider p = new GeneOntologyJsonUserDataProvider("GO.user_data.json");
		List<OrcidUserData> orcIdUserData = p.getOrcIdUserData();
		assertEquals(1, orcIdUserData.size());
		OrcidUserData data = orcIdUserData.get(0);
		assertEquals("Test Name 2", data.getScreenname());
		assertEquals("http://orcid.org/0000-0000-0000-0001", data.getOrcid());
	}
}
