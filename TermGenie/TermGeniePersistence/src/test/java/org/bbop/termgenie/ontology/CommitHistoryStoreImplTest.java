package org.bbop.termgenie.ontology;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;
import org.bbop.termgenie.presistence.EntityManagerFactoryProvider;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

public class CommitHistoryStoreImplTest {

	private static EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();
	private static File testFolder;

	@BeforeClass
	public static void beforeClass() throws IOException {
		testFolder = TempTestFolderTools.createTestFolder(CommitHistoryStoreImplTest.class);
		FileUtils.cleanDirectory(testFolder);
	}

	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testCommitHistoryStoreImpl() throws Exception {
		EntityManagerFactory emf = provider.createFactory(testFolder,
				EntityManagerFactoryProvider.HSQLDB,
				"CommitHistory");
		assertNotNull(emf);
		CommitHistoryStoreImpl store = new CommitHistoryStoreImpl(emf);

		assertNull(store.loadHistory("Test"));

		List<CommitHistoryItem> itemsForReview = store.getItemsForReview("Test");
		assertNull(itemsForReview);

		CommitHistoryItem item1 = createTestItem(1, 0, 3);
		CommitHistoryItem item2 = createTestItem(2, 3, 1);
		CommitHistoryItem item3 = createTestItem(3, 4, 2);

		store.add(item1, "Test");
		store.add(item2, "Test");
		store.add(item3, "Test");

		itemsForReview = store.getItemsForReview("Test");
		assertEquals(3, itemsForReview.size());

		itemsForReview = store.getItemsForReview("Test");
		assertEquals(3, itemsForReview.size());
		assertEqualsItem(item1, itemsForReview.get(0));
		assertEqualsItem(item2, itemsForReview.get(1));
		assertEqualsItem(item3, itemsForReview.get(2));
		
		List<Pair<String, String>> existing = store.checkRecentCommits("Test", Arrays.asList("Term label 1", "Term label -1"));
		assertEquals(1, existing.size());
		assertEquals("t:01", existing.get(0).getOne());
	}

	private CommitHistoryItem createTestItem(int i, int childrenStart, int childrenCount) {
		CommitHistoryItem item = new CommitHistoryItem();
		item.setDate(new Date());
		item.setUser("test" + i + "@test.tt");
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>(childrenCount);
		for (int c = 0; c < childrenCount; c++) {
			terms.add(createTestTerm(childrenStart + c));
		}
		item.setTerms(terms);
		return item;
	}

	private CommitedOntologyTerm createTestTerm(int i) {
		CommitedOntologyTerm t = new CommitedOntologyTerm();
		t.setId("t:0" + i);
		t.setLabel("Term label " + i);
		t.setOperation(0); // add
		t.setDefinition("Term Def " + i);
		t.setDefXRef(Arrays.asList("DefXref " + i + "_1", "DefXref " + i + "_2"));
		t.setMetaData(Collections.singletonMap("Committer", "TestCommitter " + i));
		if (i > 0) {
			CommitedOntologyTermRelation relation = new CommitedOntologyTermRelation();
			relation.setSource(t.getId());
			relation.setTarget("t:0" + (i - 1));
			relation.setTargetLabel("Term label " + (i - 1));
			Map<String, String> properties = new HashMap<String, String>();
			Relation.setType(properties, OboFormatTag.TAG_IS_A);
			relation.setProperties(properties);
			t.setRelations(Collections.singletonList(relation));
		}
		CommitedOntologyTermSynonym syn1 = new CommitedOntologyTermSynonym();
		syn1.setLabel("Syn Label " + i + "_1");
		syn1.setScope("EXACT");
		syn1.setXrefs(Collections.singleton("Syn Xref " + i + "_1"));

		CommitedOntologyTermSynonym syn2 = new CommitedOntologyTermSynonym();
		syn2.setLabel("Syn Label " + i + "_2");
		syn2.setScope("BROADER");
		List<CommitedOntologyTermSynonym> synonyms = Arrays.asList(syn1, syn2);
		t.setSynonyms(synonyms);
		return t;
	}

	private void assertEqualsItem(CommitHistoryItem expected, CommitHistoryItem actual) {
		assertEquals(expected.getUser(), actual.getUser());
		assertEquals(expected.getDate(), actual.getDate());
		assertEqualsTerms(expected.getTerms(), actual.getTerms());
	}

	private void assertEqualsTerms(List<CommitedOntologyTerm> expected,
			List<CommitedOntologyTerm> actual)
	{
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			CommitedOntologyTerm t1 = expected.get(i);
			CommitedOntologyTerm t2 = actual.get(i);
			assertEquals(t1.getId(), t2.getId());
			assertEquals(t1.getLabel(), t2.getLabel());
			assertEquals(t1.getDefinition(), t2.getDefinition());
			assertEquals(t1.getId(), t2.getId());
			assertEquals(t1.getId(), t2.getId());
			checkDefXrefs(t1, t2);
			checkSynonyms(t1, t2);
			checkRelations(t1, t2);
			assertMapEquals(t1.getMetaData(), t2.getMetaData());
		}
	}

	private void checkDefXrefs(CommitedOntologyTerm t1, CommitedOntologyTerm t2) {
		List<String> defXRef1 = t1.getDefXRef();
		if (defXRef1 != null) {
			assertArrayEquals(defXRef1.toArray(), t2.getDefXRef().toArray());
		}
		else {
			assertNull(t2.getDefXRef());
		}
	}

	private void checkSynonyms(CommitedOntologyTerm t1, CommitedOntologyTerm t2) {
		List<CommitedOntologyTermSynonym> synonyms1 = t1.getSynonyms();
		List<CommitedOntologyTermSynonym> synonyms2 = t2.getSynonyms();
		if (synonyms1 == null) {
			assertNull(synonyms2);
		}
		else if (synonyms1.isEmpty()) {
			assertTrue(synonyms2.isEmpty());
		}
		else {
			assertEquals(synonyms1.size(), synonyms2.size());
			for (int j = 0; j < synonyms1.size(); j++) {
				CommitedOntologyTermSynonym s1 = synonyms1.get(j);
				CommitedOntologyTermSynonym s2 = synonyms2.get(j);
				assertEquals(s1.getId(), s2.getId());
				assertEquals(s1.getLabel(), s2.getLabel());
				assertEquals(s1.getCategory(), s2.getCategory());
				assertEquals(s1.getScope(), s2.getScope());
				Set<String> s1Xrefs = s1.getXrefs();
				Set<String> s2Xrefs = s2.getXrefs();
				if (s1Xrefs != null) {
					assertArrayEquals(s1Xrefs.toArray(), s2Xrefs.toArray());
				}
				else {
					if (s2Xrefs != null) {
						assertTrue(s2Xrefs.isEmpty());
					}
				}
			}
		}
	}

	private void checkRelations(CommitedOntologyTerm t1, CommitedOntologyTerm t2) {
		List<CommitedOntologyTermRelation> rels1 = t1.getRelations();
		List<CommitedOntologyTermRelation> rels2 = t2.getRelations();
		if (rels1 == null) {
			if (rels2 != null) {
				assertTrue(rels2.isEmpty());
			}
		}
		else if (rels1.isEmpty()) {
			assertTrue(rels2.isEmpty());
		}
		else {
			assertEquals(rels1.size(), rels2.size());
			for (int i = 0; i < rels1.size(); i++) {
				CommitedOntologyTermRelation rel1 = rels1.get(i);
				CommitedOntologyTermRelation rel2 = rels2.get(i);
				assertEquals(rel1.getId(), rel2.getId());
				assertEquals(rel1.getSource(), rel2.getSource());
				assertEquals(rel1.getTarget(), rel2.getTarget());
				assertEquals(rel1.getTargetLabel(), rel2.getTargetLabel());
				assertMapEquals(rel1.getProperties(), rel2.getProperties());
			}
		}
	}

	private void assertMapEquals(Map<?, ?> m1, Map<?, ?> m2) {
		if (m1 == null) {
			assertNull(m2);
		}
		else if (m1.isEmpty()) {
			assertTrue(m2.isEmpty());
		}
		else {
			assertTrue(m2.keySet().containsAll(m1.keySet()));
			assertTrue(m1.keySet().containsAll(m2.keySet()));
			for(Object key : m1.keySet()) {
				assertEquals(m1.get(key), m2.get(key));
			}
		}
	}
}
