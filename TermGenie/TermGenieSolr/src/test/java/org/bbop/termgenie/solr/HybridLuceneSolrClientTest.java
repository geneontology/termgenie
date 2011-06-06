package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.junit.Test;

public class HybridLuceneSolrClientTest extends OntologyProvider {

	@Test
	public void testSuggestTerms() {
		Collection<Ontology> ontologies = Arrays.<Ontology>asList(go, pro);
		HybridLuceneSolrClient client = new HybridLuceneSolrClient(ontologies);
		
		List<OntologyTerm> terms = client.suggestTerms("exportin-T", pro, 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0).getId());
		
		terms = client.suggestTerms("exportin-T", go, 1);
		assertNotNull(terms);
		assertEquals(0, terms.size());
	}

}
