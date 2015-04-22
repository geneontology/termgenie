package org.bbop.termgenie.permissions;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.bbop.termgenie.permissions.JsonFileUserPermissionsImpl;
import org.bbop.termgenie.permissions.PermissionsData;
import org.bbop.termgenie.permissions.PermissionsData.TermGeniePermissions;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.Test;


public class PermissionsDataTest extends ResourceLoader {

	public PermissionsDataTest() {
		super(false);
	}

	@Test
	public void testPermissionsDataIO() {
		PermissionsData data1 = new PermissionsData();
		PermissionsData.TermGeniePermissions details11 = new PermissionsData.TermGeniePermissions();
		details11.addPermissions("GeneOntology", "allowWrite", "true");
		PermissionsData.TermGeniePermissions details12 = new PermissionsData.TermGeniePermissions();
		details12.addPermissions("test-ontology", "allowWrite", "true");
		details12.addPermissions("test-ontology", "screenname", "tt_P-:\")(*;");
		PermissionsData.TermGeniePermissions details13 = new PermissionsData.TermGeniePermissions();
		details13.addPermissions("globalPermissions", "allowCommitReview", "true");
		data1.userPermissions.put("user1", Collections.singletonMap("termgenie", details11));
		data1.userPermissions.put("user2", Collections.singletonMap("termgenie", details12));
		data1.userPermissions.put("user3@host.tld", Collections.singletonMap("termgenie", details13));
		
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
	
	@Test
	public void testPermissionFile() {
		PermissionsData permissions = JsonFileUserPermissionsImpl.loadInputSteam(loadResource("termgenie-user-permissions.json"));
		assertFalse(permissions.userPermissions.isEmpty());
	}

}
