package org.bbop.termgenie.ontology;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.presistence.EntityManagerFactoryProvider;
import org.bbop.termgenie.tools.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

public class CommitHistoryStoreImplTest {

	private static EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testCommitHistoryStoreImpl() throws Exception {
		EntityManagerFactory emf = provider.createFactory(folder.newFolder(),
				EntityManagerFactoryProvider.HSQLDB,
				EntityManagerFactoryProvider.MODE_DEFAULT,
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
		
		itemsForReview = store.getItemsForReview("Test");
		assertEquals(3, itemsForReview.size());
		final CommitHistoryItem change = itemsForReview.get(0);
		assertEqualsItem(item1, change);
		List<CommitedOntologyTerm> terms = change.getTerms();
		for (CommitedOntologyTerm term : terms) {
			Frame f = CommitHistoryTools.translate(term.getId(), term.getObo());
			String def = f.getTagValue(OboFormatTag.TAG_DEF, String.class);
			f.setClauses(Collections.singletonList(new Clause(OboFormatTag.TAG_DEF, "OBOSOLETE. "+def)));
			OboTools.removeAllRelations(f);
			term.setObo(OboWriterTools.writeFrame(f, null));
		}
		store.update(change, "Test");
		
		itemsForReview = store.getItemsForReview("Test");
		assertEquals(3, itemsForReview.size());
	}

	private CommitHistoryItem createTestItem(int i, int childrenStart, int childrenCount) {
		CommitHistoryItem item = new CommitHistoryItem();
		item.setDate(new Date());
		item.setCommitMessage("Test commit message: "+i);
		item.setEmail("test" + i + "@test.tt");
		item.setSavedBy("test-user-1");
		List<CommitedOntologyTerm> terms = new ArrayList<CommitedOntologyTerm>(childrenCount);
		for (int c = 0; c < childrenCount; c++) {
			terms.add(createTestTerm(childrenStart + c));
		}
		item.setTerms(terms);
		return item;
	}

	private CommitedOntologyTerm createTestTerm(int i) {
		Frame frame = OboTools.createTermFrame("t:0" + i, "Term label " + i);
		OboTools.addDefinition(frame, "Term Def " + i, Arrays.asList("DefXref " + i + "_1", "DefXref " + i + "_2"));
		frame.addClause(new Clause("comment", "TestCommitter " + i));
		if (i > 0) {
			frame.addClause(new Clause(OboFormatTag.TAG_IS_A, "t:0" + (i - 1)));
		}
		OboTools.addSynonym(frame, "Syn Label " + i + "_1", "EXACT", Collections.singleton("Syn Xref " + i + "_1"));
		OboTools.addSynonym(frame, "Syn Label " + i + "_2", "BROADER", null);
		final CommitedOntologyTerm term = CommitHistoryTools.create(frame, Modification.add, "", "test-pattern");
		return term;
	}

	private void assertEqualsItem(CommitHistoryItem expected, CommitHistoryItem actual) {
		assertEquals(expected.getCommitMessage(), actual.getCommitMessage());
		assertEquals(expected.getEmail(), actual.getEmail());
		assertEquals(expected.getSavedBy(), actual.getSavedBy());
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
			assertEquals(t1.getObo(), t2.getObo());
			assertEquals(t1.getPattern(), t2.getPattern());
		}
	}
}
