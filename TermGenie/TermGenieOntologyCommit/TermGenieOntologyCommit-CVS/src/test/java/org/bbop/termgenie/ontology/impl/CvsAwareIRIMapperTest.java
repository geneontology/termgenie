package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;


public class CvsAwareIRIMapperTest {

	private static File testFolder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testFolder = TempTestFolderTools.createTestFolder(CvsAwareIRIMapperTest.class);
		System.out.println(testFolder.getAbsolutePath());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
//		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testMapUrl() throws Exception {
		final String acceptedURL = "http://test.test/single_accepted_url";
		IRIMapper fallBackIRIMapper = new IRIMapper() {
			
			@Override
			public IRI getDocumentIRI(IRI ontologyIRI) {
				throw new RuntimeException("Unexpected call to fallback mapper with IRI: "+ontologyIRI);
			}
			
			@Override
			public URL mapUrl(String url) {
				if (acceptedURL.equals(url)) {
					try {
						return new URL(acceptedURL);
					} catch (MalformedURLException exception) {
						throw new RuntimeException(exception);
					}
				}
				throw new RuntimeException("Unexpected call to fallback mapper with URL: "+url);
			}
		};
		String cvsRoot = ":pserver:anonymous@cvs.geneontology.org:/anoncvs";
		String cvsPassword = null;
		Map<String, String> mappedCVSFiles = Collections.singletonMap("http://purl.obolibrary.org/obo/go.obo", "go/ontology/editors/gene_ontology_write.obo");
		String checkout = "go/ontology/editors";
		
		final List<String> scmMappedURLs = new ArrayList<String>();
		CvsAwareIRIMapper mapper = new CvsAwareIRIMapper(fallBackIRIMapper, cvsRoot, cvsPassword , testFolder, mappedCVSFiles, checkout) {

			@Override
			protected URL mapUrlUsingScm(String url) {
				scmMappedURLs.add(url);
				return super.mapUrlUsingScm(url);
			}
			
		};
		
		URL url = mapper.mapUrl("http://purl.obolibrary.org/obo/go.obo");
		File file = new File(url.toURI());
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.canRead());
		mapper.mapUrl("http://purl.obolibrary.org/obo/go.obo");
		mapper.mapUrl(acceptedURL);
		mapper.mapUrl(acceptedURL);
		assertEquals(2, scmMappedURLs.size());
	}

}
