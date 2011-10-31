package org.bbop.termgenie.ontology.go;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.Ontology.AbstractOntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
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
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.graph.OWLGraphWrapper.Synonym;

import com.google.inject.Injector;


public class FileOnlyGeneOntologyCommitAdapterTest {
	
	private static File testFolder = null;
	private static FileOnlyGeneOntologyCommitAdapter instance = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
		
		GoCvsHelper helper = new GoCvsHelper.GoCvsHelperAnonymous(goManager, iriMapper, cleaner, cvsFileName, cvsRoot);
		/// create adapter instance, which writes to test-local resource
		instance = new FileOnlyGeneOntologyCommitAdapter(goManager, store, helper, localFile) {

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
//		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testCommit() throws CommitException {
		List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms = createCommitTerms();
		CommitInfo commitInfo = new CommitInfo(terms, "junit-test", CommitMode.anonymus, null, null);
		CommitResult commitResult = instance.commit(commitInfo);
		assertTrue(commitResult.isSuccess());
		System.out.println(commitResult.getDiff());
	}

	private List<CommitObject<OntologyTerm<ISynonym, IRelation>>> createCommitTerms() {
		List<CommitObject<OntologyTerm<ISynonym, IRelation>>> list = new ArrayList<CommitObject<OntologyTerm<ISynonym,IRelation>>>(2);
		
		OntologyTerm<ISynonym, IRelation> term1 = createTerm(1, "GO:0003332", "Biological Process");
		OntologyTerm<ISynonym, IRelation> term2 = createTerm(2, term1.getId(), term1.getLabel());
		list.add(new CommitObject<OntologyTerm<ISynonym,IRelation>>(term2, Modification.add));
		list.add(new CommitObject<OntologyTerm<ISynonym,IRelation>>(term1, Modification.add));
		
		return list;
	}

	private OntologyTerm<ISynonym, IRelation> createTerm(final int count, final String parent, final String parentLabel) {
		List<ISynonym> synonyms = Collections.<ISynonym>singletonList(new Synonym("term syn"+count, "EXACT", null, Collections.singleton("test:syn_xref")));
		List<String> defXRef = Collections.singletonList("test:term_xref");
		Map<String, String> metaData = Collections.emptyMap();
		List<IRelation> relations = Collections.<IRelation>singletonList(new IRelation() {
			
			@Override
			public String getTargetLabel() {
				return parentLabel;
			}
			
			@Override
			public String getTarget() {
				return parent;
			}
			
			@Override
			public String getSource() {
				return "GO:faketest00"+count;
			}
			
			@Override
			public Map<String, String> getProperties() {
				return Collections.singletonMap("type", OboFormatTag.TAG_IS_A.getTag());
			}
		});
		OntologyTerm<ISynonym, IRelation> term1 = new DefaultOntologyTerm("GO:faketest00"+count, "term"+count, "def term"+count, synonyms, defXRef, metaData, relations);
		return term1;
	}

}
