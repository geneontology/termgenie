package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.junit.BeforeClass;
import org.junit.Test;

public class LuceneOnlyClientTest extends OntologyProvider {

	private static LuceneOnlyClient index;

	@BeforeClass
	public static void setupBefore() {
		List<Ontology> ontologies = Arrays.asList(go, pro, bp, cc, mf);
		List<OntologyTaskManager> managers = Arrays.asList(goManager, proManager);
		index = new LuceneOnlyClient(ontologies, managers, factory);
	}

	@Test
	public void testSuggestTermsPro() {
		List<String> terms = index.suggestTerms("exportin-T", pro, 1);
		assertNotNull(terms);
		assertEquals("PR:000017502", terms.get(0));
	}

	@Test
	public void testSuggestTermsProID() {
		List<String> terms = index.suggestTerms("PR:000017502", pro, 1);
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals("PR:000017502", terms.get(0));
	}

	@Test
	public void testSuggestTermsGO() {
		List<String> terms = index.suggestTerms("exportin-T", go, 1);
		assertNull(terms);

		int maxCount = 10;
		terms = index.suggestTerms("pig", bp, maxCount);
		assertNotNull("This may be null, if the solr server is not available.", terms);
		assertEquals(maxCount, terms.size());
		assertEquals("GO:0043473", terms.get(0)); // pigmentation

		terms = index.suggestTerms("pigmentation", cc, maxCount);
		assertNull(terms);

		terms = index.suggestTerms("pig", cc, maxCount);
		// GO:0048770 pigment granule
		assertEquals(1, terms.size());
		assertEquals("GO:0048770", terms.get(0));
	}

	@Test
	public void testSuggestTermsGOID() {
		List<String> terms = index.suggestTerms("GO:0048770", cc, 10);
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals("GO:0048770", terms.get(0));

		terms = index.suggestTerms("GO:0048770", go, 10);
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals("GO:0048770", terms.get(0));
	}

}
