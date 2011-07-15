package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.Test;

public class HybridLuceneSolrClientTest extends OntologyProvider {

	@Test
	public void testSuggestTerms() {
		Collection<OntologyTaskManager> ontologies = Arrays.<OntologyTaskManager>asList(go, pro);
		HybridLuceneSolrClient client = new HybridLuceneSolrClient(ontologies);
		
		List<OntologyTerm> terms = client.suggestTerms("exportin-T", pro.getOntology(), 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0).getId());
		
		terms = client.suggestTerms("exportin-T", go.getOntology(), 1);
		assertNotNull(terms);
		assertEquals(0, terms.size());
	}

}
