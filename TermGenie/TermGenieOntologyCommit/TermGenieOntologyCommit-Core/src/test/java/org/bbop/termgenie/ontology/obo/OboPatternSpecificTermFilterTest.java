package org.bbop.termgenie.ontology.obo;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;


public class OboPatternSpecificTermFilterTest  {

	private static OboPatternSpecificTermFilter filter;
	private static OBODoc main;
	private static OBODoc xp;
	private static List<OBODoc> allOntologies;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		Map<String, Integer> specialPatterns = Collections.singletonMap("special", Integer.valueOf(1));
		filter = new OboPatternSpecificTermFilter(specialPatterns );
		
		OBOFormatParser p = new OBOFormatParser();
		main = p.parse(new File("src/test/resources/svn-test-main.obo"));
		xp = p.parse(new File("src/test/resources/svn-test-extension.obo"));
		
		allOntologies = Arrays.asList(main, xp);
	}

	@Test
	public void testFilterTerms1a() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "default", 
				"[Term]\nid: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of FOO:0002\n" +
				"relationship: part_of FOO:0002\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered = filter.filterTerms(item, main, allOntologies, 0);
		final CommitedOntologyTerm originalTerm = terms.get(0);
		CommitedOntologyTerm filteredTerm = filtered.get(0);
		assertEquals("expected no changes to term", originalTerm, filteredTerm);
		
		// filter for xp file
		assertNull("expected no content for xp file", filter.filterTerms(item, xp, allOntologies, 1));
	}
	
	@Test
	public void testFilterTerms1b() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of FOO:0002\n" +
				"relationship: part_of FOO:0002\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		final CommitedOntologyTerm originalTerm = terms.get(0);
		CommitedOntologyTerm filteredTerm = filtered0.get(0);
		assertEquals("expected no changes to term", originalTerm, filteredTerm);
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertNull("expected no content for xp file", filtered1);
	}
	
	@Test
	public void testFilterTerms2() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\nintersection_of: FOO:0001\n" +
				"intersection_of: has_participant FOO:0002\n" +
				"relationship: has_participant FOO:0002\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n\n", 
				filtered0.get(0).getObo());
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: has_participant FOO:0002\n\n", 
				filtered1.get(0).getObo());
	}
	
	
	@Test
	public void testFilterTerms3() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of GOO:0002\n" +
				"relationship: part_of GOO:0002\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n\n", 
				filtered0.get(0).getObo());
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of GOO:0002\n\n", 
				filtered1.get(0).getObo());
	}
	
	@Test
	public void testFilterTerms4() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		CommitedOntologyTerm term = createTerm("FOO:5000", "default", 
				"[Term]\n" +
				"id: FOO:5000\n" +
				"name: foo-5000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of FOO:0002\n" +
				"relationship: part_of FOO:0002\n");
		SimpleCommitedOntologyTerm changedTerm = new SimpleCommitedOntologyTerm();
		changedTerm.setId("FOO:2001");
		changedTerm.setObo("[Term]\n" +
				"id: FOO:2001\n" +
				"name: foo-2001\n" +
				"is_a: FOO:5000\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0002\n" +
				"relationship: has_participant GOO:0002\n" +
				"relationship: part_of FOO:2000");
		List<SimpleCommitedOntologyTerm> changed = Collections.singletonList(changedTerm);
		term.setChanged(changed);
		terms.add(term);
		item.setTerms(terms);

		// filter for main
		List<CommitedOntologyTerm> filtered = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals(1, filtered.size());
		final CommitedOntologyTerm originalTerm = terms.get(0);
		CommitedOntologyTerm filteredTerm = filtered.get(0);
		assertEquals("expected no changes to term", originalTerm.getAxioms(), filteredTerm.getAxioms());
		assertEquals("expected no changes to term", originalTerm.getId(), filteredTerm.getId());
		assertEquals("expected no changes to term", originalTerm.getLabel(), filteredTerm.getLabel());
		assertEquals("expected no changes to term", originalTerm.getObo(), filteredTerm.getObo());
		assertEquals("expected no changes to term", originalTerm.getOperation(), filteredTerm.getOperation());
		assertEquals("expected no changes to term", originalTerm.getPattern(), filteredTerm.getPattern());
		assertEquals("expected no changes to term", originalTerm.getUuid(), filteredTerm.getUuid());
		List<SimpleCommitedOntologyTerm> filteredChanged = filteredTerm.getChanged();
		assertEquals(changed.size(), filteredChanged.size());
		assertEquals("[Term]\n" +
				"id: FOO:2001\n" +
				"name: foo-2001\n" +
				"is_a: FOO:5000\n" +
				"relationship: part_of FOO:2000\n\n", filteredChanged.get(0).getObo());

		// filter for xp file
		List<CommitedOntologyTerm> xpFiltered = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals(1, xpFiltered.size());
		
		CommitedOntologyTerm xpFilteredTerm = xpFiltered.get(0);
		assertEquals("expected no changes to term", originalTerm.getAxioms(), xpFilteredTerm.getAxioms());
		assertEquals("expected no changes to term", originalTerm.getId(), xpFilteredTerm.getId());
		assertEquals("expected no changes to term", originalTerm.getLabel(), xpFilteredTerm.getLabel());
		assertEquals("expected empty term", "", xpFilteredTerm.getObo());
		assertEquals("expected no changes to term", originalTerm.getOperation(), xpFilteredTerm.getOperation());
		assertEquals("expected no changes to term", originalTerm.getPattern(), xpFilteredTerm.getPattern());
		
		List<SimpleCommitedOntologyTerm> xpFilteredChanged = xpFilteredTerm.getChanged();
		assertEquals(changed.size(), xpFilteredChanged.size());
		assertEquals("[Term]\n" +
				"id: FOO:2001\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0002\n\n", xpFilteredChanged.get(0).getObo());
	}
	
	@Test
	public void testFilterTerms5a() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: has_participant FOO:0002\n" +
				"relationship: has_participant FOO:0002\n" +
				"relationship: part_of FOO:8000\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"relationship: part_of FOO:8000\n\n", 
				filtered0.get(0).getObo());
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: has_participant FOO:0002\n\n", 
				filtered1.get(0).getObo());
	}
	
	@Test
	public void testFilterTerms5b() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"relationship: part_of GOO:8000\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n\n", 
				filtered0.get(0).getObo());
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"relationship: part_of GOO:8000\n\n", 
				filtered1.get(0).getObo());
	}
	
	@Test
	public void testFilterTerms5c() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		terms.add(createTerm("FOO:4000", "special", 
				"[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of FOO:0002\n" +
				"relationship: part_of FOO:0002\n" +
				"relationship: part_of GOO:8000\n" +
				"relationship: has_participant FOO:8000\n"));
		item.setTerms(terms);
		
		// filter for main
		List<CommitedOntologyTerm> filtered0 = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals("[Term]\n" +
				"id: FOO:4000\n" +
				"name: foo-4000\n" +
				"is_a: FOO:0001\n" +
				"intersection_of: FOO:0001\n" +
				"intersection_of: part_of FOO:0002\n" +
				"relationship: part_of FOO:0002\n\n", 
				filtered0.get(0).getObo());
		
		// filter for xp file
		List<CommitedOntologyTerm> filtered1 = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals("[Term]\nid: FOO:4000\n" +
				"relationship: has_participant FOO:8000\n" +
				"relationship: part_of GOO:8000\n\n", 
				filtered1.get(0).getObo());
	}
	
	@Test
	public void testFilterTerms6() {
		// prepare
		CommitHistoryItem item = new CommitHistoryItem();
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>();
		CommitedOntologyTerm term = createTerm("FOO:6000", "default", 
				"[Term]\n" +
				"id: FOO:6000\n" +
				"name: foo-6000\n" +
				"is_a: FOO:0001\n");
		SimpleCommitedOntologyTerm changedTerm1 = new SimpleCommitedOntologyTerm();
		changedTerm1.setId("FOO:2001");
		changedTerm1.setObo("[Term]\n" +
				"id: FOO:2001\n" +
				"name: foo-2001\n" +
				"is_a: FOO:5000\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0002\n" +
				"relationship: has_participant GOO:0002\n");
		changedTerm1.setOperation(Modification.modify);
		SimpleCommitedOntologyTerm changedTerm2 = new SimpleCommitedOntologyTerm();
		changedTerm2.setId("FOO:2002");
		changedTerm2.setObo("[Term]\n" +
				"id: FOO:2002\n" +
				"name: foo-2002\n" +
				"is_a: FOO:5000\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0003\n" +
				"relationship: has_participant GOO:0003\n" +
				"relationship: part_of FOO:2001\n");
		changedTerm2.setOperation(Modification.modify);
		List<SimpleCommitedOntologyTerm> changed = Arrays.asList(changedTerm1, changedTerm2);
		term.setChanged(changed);
		terms.add(term);
		item.setTerms(terms);

		// filter for main
		List<CommitedOntologyTerm> filtered = filter.filterTerms(item, main, allOntologies, 0);
		assertEquals(1, filtered.size());
		final CommitedOntologyTerm originalTerm = terms.get(0);
		CommitedOntologyTerm filteredTerm = filtered.get(0);
		assertEquals("expected no changes to term", originalTerm.getAxioms(), filteredTerm.getAxioms());
		assertEquals("expected no changes to term", originalTerm.getId(), filteredTerm.getId());
		assertEquals("expected no changes to term", originalTerm.getLabel(), filteredTerm.getLabel());
		assertEquals("expected no changes to term", originalTerm.getObo(), filteredTerm.getObo());
		assertEquals("expected no changes to term", originalTerm.getOperation(), filteredTerm.getOperation());
		assertEquals("expected no changes to term", originalTerm.getPattern(), filteredTerm.getPattern());
		assertEquals("expected no changes to term", originalTerm.getUuid(), filteredTerm.getUuid());
		List<SimpleCommitedOntologyTerm> filteredChanged = filteredTerm.getChanged();
		assertEquals(changed.size(), filteredChanged.size());
		assertEquals("[Term]\n" +
				"id: FOO:2001\n" +
				"name: foo-2001\n" +
				"is_a: FOO:5000\n\n", filteredChanged.get(0).getObo());
		assertEquals("[Term]\n" +
				"id: FOO:2002\n" +
				"name: foo-2002\n" +
				"is_a: FOO:5000\n" +
				"relationship: part_of FOO:2001\n\n", filteredChanged.get(1).getObo());

		// filter for xp file
		List<CommitedOntologyTerm> xpFiltered = filter.filterTerms(item, xp, allOntologies, 1);
		assertEquals(1, xpFiltered.size());

		CommitedOntologyTerm xpFilteredTerm = xpFiltered.get(0);
		assertEquals("expected no changes to term", originalTerm.getAxioms(), xpFilteredTerm.getAxioms());
		assertEquals("expected no changes to term", originalTerm.getId(), xpFilteredTerm.getId());
		assertEquals("expected no changes to term", originalTerm.getLabel(), xpFilteredTerm.getLabel());
		assertEquals("expected empty term", "", xpFilteredTerm.getObo());
		assertEquals("expected no changes to term", originalTerm.getOperation(), xpFilteredTerm.getOperation());
		assertEquals("expected no changes to term", originalTerm.getPattern(), xpFilteredTerm.getPattern());

		List<SimpleCommitedOntologyTerm> xpFilteredChanged = xpFilteredTerm.getChanged();
		assertEquals(changed.size(), xpFilteredChanged.size());
		SimpleCommitedOntologyTerm xpFilteredChanged1 = xpFilteredChanged.get(0);
		SimpleCommitedOntologyTerm xpFilteredChanged2 = xpFilteredChanged.get(1);
		assertEquals("[Term]\n" +
				"id: FOO:2001\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0002\n\n", xpFilteredChanged1.getObo());
		
		assertEquals(Modification.modify, xpFilteredChanged1.getOperation());
		
		assertEquals("[Term]\n" +
				"id: FOO:2002\n" +
				"intersection_of: FOO:0003\n" +
				"intersection_of: has_participant GOO:0003\n\n", xpFilteredChanged2.getObo());
		assertEquals(Modification.modify, xpFilteredChanged2.getOperation());
	}
	
	private CommitedOntologyTerm createTerm(String id, String pattern, String obo) {
		CommitedOntologyTerm term = new CommitedOntologyTerm();
		
		term.setId(id);
		term.setPattern(pattern);
		term.setObo(obo);
		
		return term;
	}

}
