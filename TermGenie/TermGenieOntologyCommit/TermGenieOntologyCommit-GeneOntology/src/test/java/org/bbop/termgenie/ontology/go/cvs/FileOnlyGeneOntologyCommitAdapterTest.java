package org.bbop.termgenie.ontology.go.cvs;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.obo.FileOnlyOboCommitPipeline;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

import com.google.inject.Injector;


public class FileOnlyGeneOntologyCommitAdapterTest {
	
	private static File testFolder = null;
	private static FileOnlyOboCommitPipeline instance = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class<?> cls = OBOFormatWriter.NameProvider.class;
		assertNotNull(cls);
		// setup file
		testFolder = TempTestFolderTools.createTestFolder(FileOnlyGeneOntologyCommitAdapterTest.class);
		final File dbFolder = new File(testFolder, "dbfolder");
		final File cvsFolder = new File(testFolder, "work");
		final File cvsLockFile = new File(testFolder, "work.lock");
		FileUtils.write(cvsLockFile, ""); // create empty lock file
		final File cvslocalFile = new File(testFolder, "local-go.obo");
		
		// create modules for configuration
		XMLReloadingOntologyModule ontologyModule = new XMLReloadingOntologyModule("ontology-configuration_go_simple.xml", null);
		PersistenceBasicModule persistenceModule = new PersistenceBasicModule(dbFolder, null);
		AdvancedPersistenceModule advancedPersistenceModule = new AdvancedPersistenceModule("GO-ID-Manager", "go-id-manager.conf", null);
		
		// create injector from modules
		Injector injector = TermGenieGuice.createInjector(ontologyModule, persistenceModule, advancedPersistenceModule);
		
		// retrieve components (via injector) and set parameters
		ConfiguredOntology source = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager goManager = injector.getInstance(OntologyLoader.class).getOntology(source);
		IRIMapper iriMapper = injector.getInstance(IRIMapper.class);
		OntologyCleaner cleaner = injector.getInstance(OntologyCleaner.class);
		String cvsFileName = "go/ontology/editors/gene_ontology_write.obo";
		String cvsRoot = ":pserver:anonymous@cvs.geneontology.org:/anoncvs";
		String localFile = cvslocalFile.getAbsolutePath();
		CommitHistoryStore store = injector.getInstance(CommitHistoryStore.class);
		
		OboScmHelper helper = new GoCvsHelperAnonymous(goManager, iriMapper, cleaner, cvsFileName, cvsRoot);
		/// create adapter instance, which writes to test-local resource
		instance = new FileOnlyOboCommitPipeline(goManager, store, helper, localFile) {

			@Override
			protected WorkFolders createTempDir() throws CommitException {
				return new WorkFolders(cvsLockFile, cvsFolder) {

					@Override
					protected void clean() {
						// do nothing
					}
				};
			}
		};
	}
	
	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testCommit() throws CommitException {
		List<CommitObject<TermCommit>> terms = createCommitTerms();
		CommitInfo commitInfo = new CommitInfo(terms, "junit-test", CommitMode.anonymus, null, null);
		CommitResult commitResult = instance.commit(commitInfo);
		assertTrue(commitResult.isSuccess());
		System.out.println(commitResult.getDiff());
	}

	private List<CommitObject<TermCommit>> createCommitTerms() {
		List<CommitObject<TermCommit>> list = new ArrayList<CommitObject<TermCommit>>(2);
		
		Frame term1 = createTerm(1, "GO:0003332");
		Frame term2 = createTerm(2, term1.getId());
		list.add(new CommitObject<TermCommit>(new TermCommit(term2, null), Modification.add));
		list.add(new CommitObject<TermCommit>(new TermCommit(term1, null), Modification.add));
		
		return list;
	}

	private Frame createTerm(final int count, final String parent) {
		Frame frame = OboTools.createTermFrame("GO:faketest00"+count, "term"+count);
		OboTools.addSynonym(frame, "term syn"+count, "EXACT", Collections.singleton("test:syn_xref"));
		OboTools.addDefinition(frame, "def term"+count, Collections.singletonList("test:term_xref"));
		frame.addClause(new Clause(OboFormatTag.TAG_IS_A, parent));
		return frame;
	}

}
