package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.Test;

public class LuceneOnlyClientTest extends OntologyProvider {

	@Test
	public void testSuggestTerms() {
		List<Ontology> ontologies = Arrays.asList(go, pro, bp, cc, mf);
		List<OntologyTaskManager> managers = Arrays.asList(goManager, proManager);
		LuceneOnlyClient index = new LuceneOnlyClient(ontologies, managers);
		
		List<OntologyTerm> terms = index.suggestTerms("exportin-T", pro, 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0).getId());
		
		terms = index.suggestTerms("exportin-T", go, 1);
		assertNull(terms);
		
		int maxCount = 10;
		terms = index.suggestTerms("pig", bp, maxCount);
		assertNotNull("This may be null, if the solr server is not available.", terms);
		assertEquals(maxCount, terms.size());
		assertEquals("pigmentation", terms.get(0).getLabel());
		
		terms = index.suggestTerms("pigmentation", cc, maxCount);
		assertNull(terms);
		
		terms = index.suggestTerms("pig", cc, maxCount);
		// GO:0048770 pigment granule
		assertEquals(1, terms.size());
		assertEquals("GO:0048770", terms.get(0).getId());
	}

}
