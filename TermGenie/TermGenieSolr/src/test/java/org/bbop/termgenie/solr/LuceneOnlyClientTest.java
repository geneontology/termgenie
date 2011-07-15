package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.junit.Test;

public class LuceneOnlyClientTest extends OntologyProvider {

	private static Ontology otherOntology = DefaultTermTemplates.CELL_ONTOLOGY;
	
	@Test
	public void testSuggestTerms() {
		LuceneOnlyClient index = new LuceneOnlyClient(Arrays.asList(go, pro, bp, cc, mf));
		
		List<OntologyTerm> terms = index.suggestTerms("exportin-T", pro.getOntology(), 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0).getId());
		
		terms = index.suggestTerms("exportin-T", go.getOntology(), 1);
		assertNull(terms);
		
		int maxCount = 10;
		terms = index.suggestTerms("pig", bp.getOntology(), maxCount);
		assertNotNull("This may be null, if the solr server is not available.", terms);
		assertEquals(maxCount, terms.size());
		assertEquals("pigmentation", terms.get(0).getLabel());
		
		terms = index.suggestTerms("pigmentation", cc.getOntology(), maxCount);
		assertNull(terms);
		
		assertNull(index.suggestTerms("pig", otherOntology, maxCount));
	}

}
