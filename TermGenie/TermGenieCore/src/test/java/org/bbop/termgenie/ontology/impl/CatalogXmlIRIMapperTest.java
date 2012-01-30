package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.bbop.termgenie.ontology.IRIMapper;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;


public class CatalogXmlIRIMapperTest {

	@Test
	public void test() throws Exception {
		final File ONTOLOGY_FOLDER = new File("src/test/resources/ontologies");
		final File goFile = new File(ONTOLOGY_FOLDER, "gene_ontology_ext-2012-01-23.obo");
		IRIMapper fallBackIRIMapper = new IRIMapper() {
			
			@Override
			public IRI getDocumentIRI(IRI ontologyIRI) {
				URL url = mapUrl(ontologyIRI.toString());
				try {
					return IRI.create(url);
				} catch (URISyntaxException exception) {
					throw new RuntimeException(exception);
				}
			}
			
			@Override
			public URL mapUrl(String url) {
				if ("test-ontologies-catalog.xml".equals(url)) {
					try {
						final File file = new File(ONTOLOGY_FOLDER,"catalog.xml");
						return file.toURI().toURL();
					} catch (MalformedURLException exception) {
						throw new RuntimeException(exception);
					}
				}
				throw new RuntimeException("Unknown file: "+url);
			}
		};
		CatalogXmlIRIMapper mapper = new CatalogXmlIRIMapper(fallBackIRIMapper , "test-ontologies-catalog.xml");
		IRI iri = mapper.getDocumentIRI(IRI.create("http://purl.org/obo/go.obo"));
		URL url = mapper.mapUrl("http://purl.org/obo/go.obo");
		assertEquals(IRI.create(goFile).toString(), iri.toString());
		assertEquals(goFile.toURI().toURL(), url);
	}

}
