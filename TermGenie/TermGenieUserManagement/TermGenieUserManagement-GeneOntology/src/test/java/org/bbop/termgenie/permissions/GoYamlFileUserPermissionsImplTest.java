package org.bbop.termgenie.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.bbop.termgenie.tools.GitYaml;
import org.junit.Test;

public class GoYamlFileUserPermissionsImplTest {

	@Test
	public void test1() throws Exception {
		GoYamlUserPermissionsImpl instance = new GoYamlUserPermissionsImpl(new GitYaml() {
			
			@Override
			public File getYamlFile() {
				return new File("src/test/resources/user.yaml").getAbsoluteFile();
			}

			@Override
			public void update() {
				// do nothing
			}
		}, "termgenie-go");
		
		assertTrue(instance.checkPermissions("bar@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_WRITE));
		assertFalse(instance.checkPermissions("bar@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_COMMIT_REVIEW));
		assertFalse(instance.checkPermissions("bar@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_MANAGEMENT));
		assertFalse(instance.checkPermissions("bar@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM));
		assertFalse(instance.checkPermissions("bar@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL));
		
		assertTrue(instance.checkPermissions("bar2@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_WRITE));
		assertFalse(instance.checkPermissions("bar2@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_COMMIT_REVIEW));
		assertFalse(instance.checkPermissions("bar2@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_MANAGEMENT));
		assertFalse(instance.checkPermissions("bar2@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM));
		assertFalse(instance.checkPermissions("bar2@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL));
		
		assertTrue(instance.checkPermissions("foo@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_WRITE));
		assertTrue(instance.checkPermissions("foo@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_COMMIT_REVIEW));
		assertTrue(instance.checkPermissions("foo@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_MANAGEMENT));
		assertTrue(instance.checkPermissions("foo@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM));
		assertFalse(instance.checkPermissions("foo@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL));
		
		assertFalse(instance.checkPermissions("other@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_WRITE));
		assertFalse(instance.checkPermissions("other@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_COMMIT_REVIEW));
		assertFalse(instance.checkPermissions("other@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_MANAGEMENT));
		assertFalse(instance.checkPermissions("other@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM));
		assertFalse(instance.checkPermissions("other@fake.fake", GoYamlUserPermissionsImpl.FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL));
	}

	@Test
	public void test2() throws Exception {
		assertNotNull(GoYamlPermissionsTool.checkYaml(new File("src/test/resources/user.yaml"), "bla"));
	}
}
