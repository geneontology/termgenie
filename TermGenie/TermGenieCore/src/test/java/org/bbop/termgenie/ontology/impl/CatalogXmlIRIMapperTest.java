package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;


public class CatalogXmlIRIMapperTest {

	@Test
	public void test() throws Exception {
		final File ONTOLOGY_FOLDER = new File("src/test/resources/ontologies");
		final File goFile = new File(ONTOLOGY_FOLDER, "gene_ontology_ext-2012-01-23.obo");
		final File catalogFile = new File(ONTOLOGY_FOLDER,"catalog.xml");
		CatalogXmlIRIMapper mapper = new CatalogXmlIRIMapper(null , catalogFile.getAbsolutePath());
		IRI iri = mapper.getDocumentIRI(IRI.create("http://purl.org/obo/go.obo"));
		URL url = mapper.mapUrl("http://purl.org/obo/go.obo");
		assertEquals(IRI.create(goFile).toString(), iri.toString());
		assertEquals(goFile.toURI().toURL(), url);
	}

}
