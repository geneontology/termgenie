package org.bbop.termgenie.services.permissions;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.bbop.termgenie.services.permissions.PermissionsData.TermGeniePermissions;
import org.junit.Test;


public class PermissionsDataTest {

	@Test
	public void testPermissionsDataIO() {
		PermissionsData data1 = new PermissionsData();
		PermissionsData.TermGeniePermissions details = new PermissionsData.TermGeniePermissions();
		details.ontologyPermissions.put("GeneOntology", Collections.singletonMap("allowWrite", "true"));
		data1.userPermissions.put("user1", Collections.singletonMap("termgenie", details));
		data1.userPermissions.put("user2", Collections.singletonMap("termgenie", details));
		
		String json1 = PermissionsData.writeToJson(data1);
		assertNotNull(json1);
		System.out.println(json1);

		PermissionsData data2 = PermissionsData.loadFromJson(json1);
		
		assertEqualsData(data1, data2);
		
		String json2 = PermissionsData.writeToJson(data2);
		
		assertEquals(json1, json2);
	}
	
	private void assertEqualsData(PermissionsData d1, PermissionsData d2) {
		assertNotNull(d1);
		assertNotNull(d2);
		assertEquals(d1.userPermissions.size(), d2.userPermissions.size());
		assertTrue(d2.userPermissions.keySet().containsAll(d1.userPermissions.keySet()));
		for(String guid : d1.userPermissions.keySet()) {
			Map<String, TermGeniePermissions> app1 = d1.userPermissions.get(guid);
			Map<String, TermGeniePermissions> app2 = d2.userPermissions.get(guid);
			assertNotNull(app1);
			assertNotNull(app2);
			assertEquals(app1.size(), app2.size());
			assertTrue(app2.keySet().containsAll(app1.keySet()));
			for(String app : app1.keySet()) {
				TermGeniePermissions perm1 = app1.get(app);
				TermGeniePermissions perm2 = app2.get(app);
				assertNotNull(perm1);
				assertNotNull(perm2);
				assertEquals(perm1.ontologyPermissions.size(), perm2.ontologyPermissions.size());
				assertTrue(perm2.ontologyPermissions.keySet().containsAll(perm1.ontologyPermissions.keySet()));
				for(String ont : perm1.ontologyPermissions.keySet()) {
					Map<String, String> ont1 = perm1.ontologyPermissions.get(ont);
					Map<String, String> ont2 = perm2.ontologyPermissions.get(ont);
					assertNotNull(ont1);
					assertNotNull(ont2);
					assertTrue(ont2.keySet().containsAll(ont1.keySet()));
					for(String flag : ont1.keySet()) {
						assertEquals(ont1.get(flag), ont2.get(flag));
					}
				}
			}
		}
	}

}
