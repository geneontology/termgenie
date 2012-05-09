package org.bbop.termgenie.svn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitHistoryStoreImpl;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCleaner.NoopOntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboPatternSpecificTermFilter;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.presistence.EntityManagerFactoryProvider;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.bbop.termgenie.tools.ResourceLoader;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.bbop.termgenie.tools.Triple;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;

import owltools.graph.OWLGraphWrapper;


public class SvnCommitTest extends TempTestFolderTools {

	private static File testFolder;
	private static Triple<SvnTool, SVNURL, ISVNAuthenticationManager> svnTools;

	@BeforeClass
	public static void beforeClass() throws Exception {
		testFolder = createTestFolder(SvnCommitTest.class, true);
		
		// general layout
		File repositoryDirectory = new File(testFolder, "repository");
		FileUtils.forceMkdir(repositoryDirectory);
		
		File stagingDirectory = new File(testFolder, "staging");
		FileUtils.forceMkdir(stagingDirectory);
		
		File readOnlyDirectory = new File(testFolder, "readonly");
		FileUtils.forceMkdir(readOnlyDirectory);
		
		TestResourceLoader loader = new TestResourceLoader();
		
		// file staging with content
		File trunk = new File(stagingDirectory, "trunk");
		trunk.mkdirs();
		File ontologyFolder = new File(trunk, "ontology");
		ontologyFolder.mkdirs();
		File ontologyFile = new File(ontologyFolder, "svn-test-main.obo");
		loader.copyResource("svn-test-main.obo", ontologyFile);
		
		File extensionFolder = new File(trunk, "extensions");
		extensionFolder.mkdirs();
		File extensionFile = new File(extensionFolder, "svn-test-extension.obo");
		loader.copyResource("svn-test-extension.obo", extensionFile);
		
		// create repository and java adapter
		svnTools = SvnRepositoryTools.createLocalRepository(repositoryDirectory, stagingDirectory, readOnlyDirectory);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
//		deleteTestFolder(testFolder);
	}
	
	@Test
	public void simpleCommit() throws Exception {
		Ontology ontology = new Ontology("foo", null, Arrays.asList("FOO:0001", "FOO:1000")) {
			// intentionally empty
		};
		OntologyTaskManager source = loadOntology(ontology);
		
		CommitHistoryStore store = createTestStore(ontology);
		
		IRIMapper iriMapper = new NoopIRIMapper();
		OntologyCleaner cleaner = new NoopOntologyCleaner();
		
		OboScmHelper helper = new TestSvnHelper(iriMapper, cleaner, "trunk/ontology/svn-test-main.obo", Collections.singletonList("trunk/extensions/svn-test-extension.obo"), svnTools.getTwo(), svnTools.getThree());

		// create commit pipeline with custom temp folder
		final File checkoutDirectory = new File(testFolder, "checkout");
		FileUtils.forceMkdir(checkoutDirectory);
		TermFilter<OBODoc> filter = new OboPatternSpecificTermFilter(Collections.singletonMap("test-pattern", 1)){

			@Override
			protected boolean isExternalIdentifier(String id, Frame frame) {
				return id.startsWith("FOO:1");
			}
		};
		OboCommitReviewPipeline p = new OboCommitReviewPipeline(source, store, filter, new NoopReviewMailHandler(), helper) {

			@Override
			protected WorkFolders createTempDir() throws CommitException {
				return new WorkFolders(null, checkoutDirectory) {

					@Override
					protected void clean() {
						// do nothing
					}
				};
			}
		};
		
		BeforeReview beforeReview = p.getBeforeReview();
		
		List<CommitHistoryItem> items = beforeReview.getItemsForReview();
		assertEquals(1, items.size());
		CommitHistoryItem item = items.get(0);
		assertFalse(item.isCommitted());
		final int commitID = item.getId();
		
		GenericTaskManager<AfterReview> afterReviewManager = p.getAfterReview();
		afterReviewManager.runManagedTask(new ManagedTask<AfterReview>(){

			@Override
			public Modified run(AfterReview afterReview)
			{
				try {
					List<CommitResult> results = afterReview.commit(Collections.singletonList(commitID), ProcessState.NO);
					assertEquals(1, results.size());
					
					CommitResult commitResult = results.get(0);
					assertTrue(commitResult.isSuccess());
					
				} catch (CommitException exception) {
					throw new RuntimeException(exception);
				}
				return null;
			}
			
		});
	
		
		SvnTool svnTool = new SvnTool(new File(testFolder, "verification"), svnTools.getTwo(), svnTools.getThree());
		svnTool.connect();
		boolean checkout = svnTool.checkout(Arrays.asList("trunk/ontology/svn-test-main.obo", "trunk/extensions/svn-test-extension.obo"), ProcessState.NO);
		svnTool.close();
		assertTrue(checkout);
		
		OBOFormatParser parser = new OBOFormatParser();
		File local = svnTool.getTargetFolder();
		OBODoc mainObo = parser.parse(new File(local, "trunk/ontology/svn-test-main.obo"));
		Frame termFrame = mainObo.getTermFrame("FOO:2001");
		assertNotNull(termFrame);
		assertTrue(termFrame.getClauses(OboFormatTag.TAG_INTERSECTION_OF).isEmpty());
		assertTrue(termFrame.getClauses(OboFormatTag.TAG_RELATIONSHIP).isEmpty());
		
		Frame termFrame2 = mainObo.getTermFrame("FOO:0005");
		assertNotNull(termFrame2);
		
		OBODoc extensionObo = parser.parse(new File(local, "trunk/extensions/svn-test-extension.obo"));
		Frame extensionTermFrame = extensionObo.getTermFrame("FOO:2001");
		assertNotNull(extensionTermFrame);
		assertEquals(2, extensionTermFrame.getClauses(OboFormatTag.TAG_INTERSECTION_OF).size());
		assertEquals(0, extensionTermFrame.getClauses(OboFormatTag.TAG_RELATIONSHIP).size());
		
		assertNull(extensionObo.getTermFrame("FOO:0005"));
	}

	private OntologyTaskManager loadOntology(Ontology ontology) {
		final SvnTool svnTool = svnTools.getOne();
		try {
			svnTool.connect();
			boolean checkout = svnTool.checkout(Arrays.asList("trunk/ontology/svn-test-main.obo", "trunk/extensions/svn-test-extension.obo"), ProcessState.NO);
			svnTool.close();
			assertTrue(checkout);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		OntologyTaskManager source = new OntologyTaskManager(ontology) {
			
			@Override
			protected OWLGraphWrapper createManaged() {
				try {
					OBOFormatParser p = new OBOFormatParser();
					svnTool.connect();
					boolean update = svnTool.update(Arrays.asList("trunk/ontology/svn-test-main.obo", "trunk/extensions/svn-test-extension.obo"), ProcessState.NO);
					assertTrue(update);
					svnTool.close();
					File local = svnTool.getTargetFolder();
					OBODoc mainObo = p.parse(new File(local, "trunk/ontology/svn-test-main.obo"));
					OBODoc extensionObo = p.parse(new File(local, "trunk/extensions/svn-test-extension.obo"));
					
					mainObo.mergeContents(extensionObo);
					Obo2Owl obo2Owl = new Obo2Owl();
					OWLOntology owlOntology = obo2Owl.convert(mainObo);
					return new OWLGraphWrapper(owlOntology);
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}

			@Override
			protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) {
				return createManaged();
			}
		};
		
		
		
		return source;
	}

	private CommitHistoryStore createTestStore(Ontology ontology)
			throws CommitHistoryStoreException
	{
		EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();
		
		CommitHistoryStore store = new CommitHistoryStoreImpl(provider.createFactory(new File(testFolder, "db"), EntityManagerFactoryProvider.HSQLDB, EntityManagerFactoryProvider.MODE_DEFAULT, "SvnTests"));
		
		/*
		 [Term]
		 id: FOO:2001
		 name: foo-2001
		 is_a: FOO:0003
		 intersection_of: FOO:0003 
		 intersection_of: part_of FOO:1002 
		 relationship: part_of FOO:1002 
		*/
		Frame frame = new Frame(FrameType.TERM);
		frame.setId("FOO:2001");
		frame.addClause(new Clause(OboFormatTag.TAG_ID, "FOO:2001"));
		frame.addClause(new Clause(OboFormatTag.TAG_NAME, "foo-2001"));
		frame.addClause(new Clause(OboFormatTag.TAG_IS_A, "FOO:0003"));
		frame.addClause(new Clause(OboFormatTag.TAG_INTERSECTION_OF, "FOO:0003"));
		
		Clause cl_i = new Clause(OboFormatTag.TAG_INTERSECTION_OF);
		cl_i.addValue("part_of");
		cl_i.addValue("FOO:1002");
		frame.addClause(cl_i);
		
		Clause cl_r = new Clause(OboFormatTag.TAG_RELATIONSHIP);
		cl_r.addValue("part_of");
		cl_r.addValue("FOO:1002");
		frame.addClause(cl_r);
		
		CommitedOntologyTerm term = CommitHistoryTools.create(frame, Modification.add, OwlStringTools.translateAxiomsToString(Collections.<OWLAxiom>emptySet()), "test-pattern");
		
		/*
		 [Term]
		 id: FOO:0005
		 name: foo-0005
		 is_a: FOO:0004
		*/
		Frame frame2 = new Frame(FrameType.TERM);
		frame2.setId("FOO:0005");
		frame2.addClause(new Clause(OboFormatTag.TAG_ID, "FOO:0005"));
		frame2.addClause(new Clause(OboFormatTag.TAG_NAME, "foo-0005"));
		frame2.addClause(new Clause(OboFormatTag.TAG_IS_A, "FOO:0004"));
		CommitedOntologyTerm term2 = CommitHistoryTools.create(frame2, Modification.add, OwlStringTools.translateAxiomsToString(Collections.<OWLAxiom>emptySet()), "other-pattern");
		
		
		List<CommitedOntologyTerm> terms = Arrays.asList(term, term2);
		CommitHistoryItem item = new CommitHistoryItem();
		item.setCommitMessage("Test commit #1");
		item.setCommitted(false);
		item.setDate(new Date());
		item.setEmail("foo@foo.bar");
		item.setSavedBy("foobar");
		item.setTerms(terms);
		store.add(item, ontology.getUniqueName());
		return store;
	}
	
	
	private static final class NoopIRIMapper implements IRIMapper {

		@Override
		public IRI getDocumentIRI(IRI ontologyIRI) {
			return ontologyIRI;
		}

		@Override
		public URL mapUrl(String url) {
			try {
				return new URL(url);
			} catch (MalformedURLException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	private static final class TestSvnHelper extends OboScmHelper {

		private final SVNURL repositoryURL;
		private final ISVNAuthenticationManager authManager;

		private TestSvnHelper(IRIMapper iriMapper,
				OntologyCleaner cleaner,
				String mainOntology,
				List<String> otherOntologyFileNames,
				SVNURL repositoryURL,
				ISVNAuthenticationManager authManager)
		{
			super(iriMapper, cleaner, mainOntology, otherOntologyFileNames);
			this.repositoryURL = repositoryURL;
			this.authManager = authManager;
		}

		@Override
		public boolean isSupportAnonymus() {
			return false;
		}

		@Override
		public String getCommitUserName() {
			return "";
		}

		@Override
		public String getCommitPassword() {
			return "";
		}

		@Override
		public CommitMode getCommitMode() {
			return CommitMode.internal;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File scmFolder) throws CommitException
		{
			return new SvnTool(scmFolder, repositoryURL, authManager);
		}
	}

	private static class TestResourceLoader extends ResourceLoader {

		protected TestResourceLoader() {
			super(false);
		}
		
		public void copyResource(String resource, File target) throws IOException {
			InputStream input = loadResource(resource);
			OutputStream output = new FileOutputStream(target);
			IOUtils.copy(input, output);
			input.close();
			output.close();
		}
	}
}
