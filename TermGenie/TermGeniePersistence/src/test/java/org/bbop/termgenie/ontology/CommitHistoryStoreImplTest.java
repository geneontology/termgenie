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

import javax.persistence.EntityManagerFactory;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;
import org.bbop.termgenie.presistence.EntityManagerFactoryProvider;
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
	}

	private CommitHistoryItem createTestItem(int i, int childrenStart, int childrenCount) {
		CommitHistoryItem item = new CommitHistoryItem();
		item.setDate(new Date());
		item.setUser("test"+i);
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
}
