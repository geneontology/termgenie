package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.IRI;

public class FileCachingIRIMapperTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testCaching() throws Exception {
		String localCache = folder.newFolder().getAbsolutePath();
		final List<String> requests = new ArrayList<String>();
		FileCachingIRIMapper mapper = new FileCachingIRIMapper(localCache, 6L, TimeUnit.HOURS) {

			@Override
			protected InputStream getInputStream(IRI iri) throws IOException {
				String s = iri.toURI().toASCIIString();
				requests.add(s);
				return new ByteArrayInputStream(s.getBytes());
			}

			/*
			 * (non-Javadoc)
			 * @see
			 * org.bbop.termgenie.ontology.impl.FileCachingIRIMapper#reloadIRIs
			 * ()
			 */
			@Override
			protected void reloadIRIs() {
				super.reloadIRIs();
				requests.add("reload");
			}

		};

		// query of the url is ignored for caching, use it a request indicator
		// for the test
		mapper.getDocumentIRI(IRI.create("http://foo.bar/path1?q=1"));
		mapper.getDocumentIRI(IRI.create("http://foo.bar/path1?q=2"));
		mapper.reloadIRIs();
		mapper.getDocumentIRI(IRI.create("http://foo.bar/path1?q=3"));

		assertEquals(3, requests.size());
		assertEquals("http://foo.bar/path1?q=1", requests.get(0));
		assertEquals("reload", requests.get(1));
		assertEquals("http://foo.bar/path1?q=3", requests.get(2));
	}

	@Test
	public void testAutoReload() throws Exception {
		String localCache = folder.newFolder().getAbsolutePath();
		final List<String> requests = new ArrayList<String>();
		new FileCachingIRIMapper(localCache, 200L, TimeUnit.MILLISECONDS) {

			private int count = 0;

			@Override
			protected void reloadIRIs() {
				requests.add("reload" + count);
				count += 1;
			}

		};
		Thread.sleep(700L);

		assertEquals(3, requests.size());
		assertEquals("reload0", requests.get(0));
		assertEquals("reload1", requests.get(1));
		assertEquals("reload2", requests.get(2));
	}

	@Test
	public void testLocalCacheFilename() throws Exception {
		assertEquals("www.foo.bar/test.obo",
				FileCachingIRIMapper.localCacheFilename(IRI.create("http://www.foo.bar/test.obo")));
	}

	@Test
	public void testEscapeToBuffer() {
		StringBuilder sb = new StringBuilder();
		FileCachingIRIMapper.escapeToBuffer(sb, "/t?ui+7.obo");
		assertEquals(File.separator + "t_ui_7.obo", sb.toString());
	}
	
	@Test
	public void testGetInputStream() throws Exception {
		String localCache = folder.newFolder().getAbsolutePath();
		FileCachingIRIMapper m = new FileCachingIRIMapper(localCache , 1L, TimeUnit.HOURS);
		IRI iri = IRI.create("https://raw.githubusercontent.com/geneontology/termgenie/master/TermGenie/TermGenieCore/pom.xml");
		InputStream inputStream = m.getInputStream(iri);
		String content = IOUtils.toString(inputStream);
		assertTrue(content.startsWith("<project "));
	}
}
