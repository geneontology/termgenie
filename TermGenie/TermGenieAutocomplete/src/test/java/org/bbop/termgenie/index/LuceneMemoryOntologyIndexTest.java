package org.bbop.termgenie.index;

import static org.junit.Assert.*;

import java.util.Collection;

import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerFactoryImpl;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;


public class LuceneMemoryOntologyIndexTest {

	@Test
	public void testLuceneMemoryOntologyIndex() throws Exception {
		ParserWrapper pw = new ParserWrapper();
		OWLGraphWrapper g = pw.parseToOWLGraph("http://purl.obolibrary.org/obo/go.owl");

		ReasonerFactory factory = new ReasonerFactoryImpl();
		LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(g, null, null, null, factory);
		Collection<SearchResult> results = index.search(" me  pigmentation ", 5, null);
		for (SearchResult searchResult : results) {
			TermSuggestion suggestion = searchResult.term;
			String id = suggestion.getIdentifier();
			assertNotNull(suggestion.getLabel());
			assertNotNull(suggestion.getDescription());
			OWLObject owlObject = g.getOWLObjectByIdentifier(id);
			String label = g.getLabel(owlObject);
			System.out.println(id + "  " + searchResult.score + "  " + label);
		}
		index.close();
		assertEquals(2, results.size());
	}

}
