package org.bbop.termgenie.user.go;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.bbop.termgenie.user.UserData;
import org.junit.Test;


public class GeneOntologyUserDataProviderTest {

	@Test
	public void testGetUserDataPerEMail() {
		String gocConfigResource = "GO.curators_dbxrefs";
		String gocMappingResource = "GO.email_dbxrefs.test";
		GeneOntologyUserDataProvider provider = new GeneOntologyUserDataProvider(gocConfigResource, '\t', gocMappingResource, '\t');
		UserData userData = provider.getUserDataPerEMail("test@test.test");
		assertEquals("test@test.test", userData.getGuid());
		assertEquals("test", userData.getScmAlias());
		assertEquals("GOC:test", userData.getXref());
		
		userData = provider.getUserDataPerEMail("no@no.no");
		assertNull(userData.getXref());
	}

	@Test
	public void testGetUserDataPerGuid() {
		String gocConfigResource = "GO.curators_dbxrefs";
		String gocMappingResource = "GO.email_dbxrefs.test";
		GeneOntologyUserDataProvider provider = new GeneOntologyUserDataProvider(gocConfigResource, '\t', gocMappingResource, '\t');
		UserData userData = provider.getUserDataPerGuid("test-guid", Arrays.asList("no@no.no", "test2@test.test"));
		assertEquals("Chris Mungall", userData.getScreenname());
		assertEquals("test-guid", userData.getGuid());
		assertEquals("cjm", userData.getScmAlias());
	}

	@Test
	public void testSplitLine() {
		assertArrayEquals(new String[0], GeneOntologyUserDataProvider.splitLine(null, ';').toArray());
		assertArrayEquals(new String[]{""}, GeneOntologyUserDataProvider.splitLine("", ';').toArray());
		assertArrayEquals(new String[]{"", ""}, GeneOntologyUserDataProvider.splitLine(";", ';').toArray());
		assertArrayEquals(new String[]{"", "", ""}, GeneOntologyUserDataProvider.splitLine(";;", ';').toArray());
		
		assertArrayEquals(new String[]{"", "b", ""}, GeneOntologyUserDataProvider.splitLine(";b;", ';').toArray());
		assertArrayEquals(new String[]{"a", "bbb", "c"}, GeneOntologyUserDataProvider.splitLine("a;bbb;c", ';').toArray());
		assertArrayEquals(new String[]{"aaa", "bbb", "ccc"}, GeneOntologyUserDataProvider.splitLine("aaa;bbb;ccc", ';').toArray());
	}

}
