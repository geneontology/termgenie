package org.bbop.termgenie.user.go;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.go.GoYamlUserDataProvider.Md5UserData;
import org.junit.Test;

public class GoYamlUserDataProviderTest {

	@Test
	public void test() throws Exception {
		GoYamlUserDataProvider p = new GoYamlUserDataProvider(new File("src/test/resources/user.yaml").getCanonicalPath());
		
		UserData data = p.getUserDataPerEMail("foo@fake.fake");
		assertNotNull(data);
		assertEquals("Foo User", data.getScreenname());
		assertEquals("GOC:foo", data.getXref());
		assertEquals("http://orcid.org/0000-0000-0000-0002", data.getOrcid());
		assertEquals("foo@fake.fake", data.getEmail());
		assertEquals("foo@fake.fake", data.getGuid());
		assertEquals("foo", data.getScmAlias());
	}

	@Test
	public void test2() throws Exception {
		List<Md5UserData> userData = GoYamlUserDataProvider.loadUserData(new File("src/test/resources/user.yaml"));
		assertEquals(2, userData.size());
	}
}
