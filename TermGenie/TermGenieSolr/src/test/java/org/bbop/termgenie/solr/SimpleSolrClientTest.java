package org.bbop.termgenie.solr;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.junit.Test;

/**
 * Tests for {@link SimpleSolrClient}.
 */
public class SimpleSolrClientTest {

	private static SimpleSolrClient client = new SimpleSolrClient();
	private static Ontology ontology = DefaultTermTemplates.GENE_ONTOLOGY;
	
	private static Ontology otherOntology = DefaultTermTemplates.CELL_ONTOLOGY;
	
	/**
	 *  Tests for {@link SimpleSolrClient#suggestTerms(String, Ontology, int)}.
	 */
	@Test
	public void testSuggestTerms() {
		String query = "pig";
		int maxCount = 10;
		List<OntologyTerm> terms = client.suggestTerms(query, ontology, maxCount);
		assertEquals(maxCount, terms.size());
		assertEquals("pigmentation", terms.get(0).getLabel());
		assertNull(client.suggestTerms(query, otherOntology, maxCount));
	}

	/**
	 * Tests for {@link SimpleSolrClient#searchGeneOntologyTerms(String, String, int)}
	 */
	@Test
	public void testSearchGeneOntologyTerms() {
		String query = "nucl";
		String branch = "cellular_component";
		int maxCount = 5;
		List<OntologyTerm> terms = client.searchGeneOntologyTerms(query, branch, maxCount);
		assertEquals(maxCount, terms.size());
		assertEquals("nucleus", terms.get(0).getLabel());
	}

	/**
	 * Tests for {@link SimpleSolrClient#sortbyLabelLength(List)}
	 */
	@Test
	public void testSortbyLabelLength() {
		List<SolrDocument> solrDocuments = new ArrayList<SolrDocument>();
		solrDocuments.add(createDoc("longer"));
		solrDocuments.add(createDoc("longest"));
		solrDocuments.add(createDoc("short"));
		client.sortbyLabelLength(solrDocuments);
		assertEqualsLabel("short", solrDocuments.get(0));
		assertEqualsLabel("longer", solrDocuments.get(1));
		assertEqualsLabel("longest", solrDocuments.get(2));
	}

	static void assertEqualsLabel(String expected, SolrDocument document) {
		assertEquals(expected, document.getFieldValue("label").toString());
	}
	
	static SolrDocument createDoc(String label) {
		SolrDocument doc = new SolrDocument();
		doc.addField("label", label);
		return doc;
	}
}
