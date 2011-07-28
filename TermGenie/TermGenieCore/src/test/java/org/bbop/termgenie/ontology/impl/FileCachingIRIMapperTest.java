package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileCachingIRIMapperTest {

	private static File testFolder;
	
	@BeforeClass
	public static void beforeClass() {
		testFolder = TestFolderTools.createTestFolder(FileCachingIRIMapperTest.class);
	}
	
	@AfterClass
	public static void afterClass() {
		TestFolderTools.deleteTestFolder(testFolder);
	}
	
	@Test
	public void testCaching() throws Exception {
		String localCache = testFolder.getAbsolutePath();
		final List<String> requests = new ArrayList<String>();
		FileCachingIRIMapper mapper = new FileCachingIRIMapper(localCache, 6L, TimeUnit.HOURS) {

			@Override
			protected InputStream getInputStream(URL url) throws IOException {
				String s = url.toExternalForm();
				requests.add(s);
				return new StringInputStream(s);
			}

			/* (non-Javadoc)
			 * @see org.bbop.termgenie.ontology.impl.FileCachingIRIMapper#reloadIRIs()
			 */
			@Override
			protected void reloadIRIs() {
				super.reloadIRIs();
				requests.add("reload");
			}
			
			
		};
		
		// query of the url is ignored for caching, use it a request indicator for the test
		mapper.mapUrl("http://foo.bar/path1?q=1"); 
		mapper.mapUrl("http://foo.bar/path1?q=2");
		mapper.reloadIRIs();
		mapper.mapUrl("http://foo.bar/path1?q=3");
		
		assertEquals(3, requests.size());
		assertEquals("http://foo.bar/path1?q=1", requests.get(0));
		assertEquals("reload", requests.get(1));
		assertEquals("http://foo.bar/path1?q=3", requests.get(2));
	}
	
	@Test
	public void testLocalCacheFilename() throws Exception {
		assertEquals("www.foo.bar/test.obo", FileCachingIRIMapper.localCacheFilename(new URL("http://www.foo.bar/test.obo")));
	}

	@Test
	public void testEscapeToBuffer() {
		StringBuilder sb = new StringBuilder();
		FileCachingIRIMapper.escapeToBuffer(sb, "/t?ui+7.obo");
		assertEquals(File.separator+"t_ui_7.obo", sb.toString());
	}

}
