package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class HybridLuceneSolrClientTest extends OntologyProvider {

	@Test
	public void testSuggestTerms() {
		Collection<OntologyTaskManager> ontologies = Arrays.<OntologyTaskManager> asList(goManager,
				proManager);
		HybridLuceneSolrClient client = new HybridLuceneSolrClient(ontologies, factory);

		List<String> terms = client.suggestTerms("exportin-T", pro, 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0));

		terms = client.suggestTerms("exportin-T", go, 1);
		assertNotNull(terms);
		assertEquals(0, terms.size());
	}

}
