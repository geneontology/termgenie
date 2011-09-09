package org.bbop.termgenie.ontology.go;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.impl.BaseOntologyLoader;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GeneOntologyCommitAdapter implements Committer {

	private static final Logger logger = Logger.getLogger(GeneOntologyCommitAdapter.class);

	private final ConfiguredOntology go;
	private final DirectOntologyLoader loader;
	private final ReasonerFactory reasonerFactory;
	private final String cvsOntologyFileName;
	private final String cvsRoot;
	private final String cvsPassword;
	private final CommitHistoryStore store;

	@Inject
	GeneOntologyCommitAdapter(@Named("ConfiguredOntologyGeneOntology") ConfiguredOntology source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named(ReasonerModule.NAMED_DIRECT_REASONER_FACTORY) ReasonerFactory reasonerFactory,
			@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
			@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot,
			@Named("GeneOntologyCommitAdapterCVSPassword") String cvsPassword,
			CommitHistoryStore store)
	{
		super();
		this.go = source;
		this.reasonerFactory = reasonerFactory;
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
		this.cvsPassword = cvsPassword;
		loader = new DirectOntologyLoader(iriMapper, cleaner);
		this.store = store;
	}

	@Override
	public boolean commit(CommitInfo commitInfo) throws CommitException {
		List<CommitObject<OntologyTerm>> terms = commitInfo.getTerms();
		if (terms != null && !terms.isEmpty()) {
			if (commitInfo.getCommitMode() == CommitMode.explicit) {
				if (commitInfo.getUsername() == null) {
					throw new CommitException("If explicit mode is selected, an username is required.", true);
				}
				if (commitInfo.getPassword() == null) {
					throw new CommitException("If explicit mode is selected, a password is required.", true);
				}
			}
			else if (commitInfo.getCommitMode() == CommitMode.anonymus) {
				throw new CommitException("Anonymus mode is not supported for the GeneOntology commit.", true);
			}
			return commitInternal(commitInfo);
		}
		return false;
	}

	private boolean commitInternal(CommitInfo commitInfo) throws CommitException {
		List<CommitObject<OntologyTerm>> terms = commitInfo.getTerms();
		List<CommitObject<Relation>> relations = commitInfo.getRelations();

		CVSTools cvs = null;
		
		// TODO this does not create a proper temp dir, 
		// TODO use locking and create a unique tempdir using a timestamp
		File tempDirectory = FileUtils.getTempDirectory();  
		try {
			File cvsFolder;
			try {
				cvsFolder = new File(tempDirectory, "cvs");
				FileUtils.forceMkdir(cvsFolder);
			} catch (IOException exception) {
				String message = "Could not create working directories for the commit";
				throw new CommitException(message, exception, true);
			}
			
			cvs = createCVS(commitInfo, cvsFolder);
			
			File oboFolder;
			try {
				oboFolder = new File(tempDirectory, "obo");
				FileUtils.forceMkdir(oboFolder);
			} catch (IOException exception) {
				String message = "Could not create working directories for the commit";
				throw new CommitException(message, exception, true);
			}
			
			try {
				// cvs checkout
				cvs.checkout(cvsOntologyFileName);
			} catch (IOException exception) {
				String message = "Could not checkout recent copy of the ontology";
				throw new CommitException(message, exception, true);
			}
			
			File cvsGoFile = new File(cvsFolder, cvsOntologyFileName);
			OWLGraphWrapper ontology = loadOntology(cvsGoFile);
			
			// apply changes

			// TODO
			
			// reason and check validity
			// only throw error if there is an inconsistency,
			// which can not be recovered silently!
			ReasonerTaskManager reasonerManager = reasonerFactory.getDefaultTaskManager(ontology);
			
			// TODO
			
			File oboFile = createOBOFile(oboFolder, ontology);
			
			CommitHistory history;
			CommitHistoryItem historyItem;
			try {
				// add terms to local commit log
				history = store.loadAll(go.getUniqueName());
				Date date = new Date();
				String user = commitInfo.getUsername();
				if (history == null) {
					history = CommitHistoryTools.create(terms, relations, user, date);
					historyItem = history.getItems().get(0);
				}
				else {
					historyItem = CommitHistoryTools.add(history, terms, relations, user, date);
				}
				store.store(history);
			} catch (CommitHistoryStoreException exception) {
				String message = "Problems handling commit history for ontology: "+ontology;
				throw new CommitException(message, exception, true);
			}
			

			copyOBOFileForCommit(cvsGoFile, oboFile);

			try {
				// commit cvs changes
				cvs.commit("TermGenie commit for user: " + commitInfo.getTermgenieUser());
			} catch (IOException exception) {
				throw new CommitException("Error during CVS commit", exception, false);
			}

			try {
				// set terms in commit log as committed
				historyItem.setCommitted(true);
				store.store(history);
			} catch (CommitHistoryStoreException exception) {
				String message = "Problems handling commit history for ontology: "+ontology;
				throw new CommitException(message, exception, false);
			}
			return true;
		}
		finally {
			try {
				// clean up
				FileUtils.deleteDirectory(tempDirectory);
			} catch (IOException exception) {
				logger.warn("Could not clear temp directory: " + tempDirectory.getAbsolutePath(),
						exception);
			}
		}
	}

	private void copyOBOFileForCommit(File cvsGoFile, File oboFile) throws CommitException {
		try {
			// copy changed ontology over cvs checkout
			FileUtils.copyFile(oboFile, cvsGoFile);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to commit file";
			throw new CommitException(message, exception, true);
		}
	}

	private File createOBOFile(File oboFolder, OWLGraphWrapper ontology) throws CommitException {
		File oboFile;
		try {
			// write OBO file to temp
			Owl2Obo converter = new Owl2Obo();
			OBODoc oboDoc = converter.convert(ontology.getSourceOntology());
			OBOFormatWriter writer = new OBOFormatWriter();
			
			oboFile = new File(oboFolder, "go.obo");
			writer.write(oboDoc, new BufferedWriter(new FileWriter(oboFile)));
		} catch (OWLOntologyCreationException exception) {
			String message = "Could not convert ontology to OBO";
			throw new CommitException(message, exception, true);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to file";
			throw new CommitException(message, exception, true);
		}
		return oboFile;
	}

	private OWLGraphWrapper loadOntology(File cvsGoFile) throws CommitException {
		OWLGraphWrapper ontology;
		try {
			// load OBO
			String localSource = cvsGoFile.getAbsolutePath();
			ConfiguredOntology config = ConfiguredOntology.createCopy(go, localSource);
			ontology = loader.load(config);
		} catch (OWLOntologyCreationException exception) {
			String message = "Could load recent copy of the ontology";
			throw new CommitException(message, exception, true);
		} catch (IOException exception) {
			String message = "Could load recent copy of the ontology";
			throw new CommitException(message, exception, true);
		}
		return ontology;
	}

	private CVSTools createCVS(CommitInfo commitInfo, File cvsFolder) {
		String realPassword;
		if (commitInfo.getCommitMode() == CommitMode.internal) {
			realPassword = cvsPassword;
		}
		else {
			realPassword = commitInfo.getPassword();
		}
		CVSTools cvs = new CVSTools(cvsRoot, realPassword, cvsFolder);
		return cvs;
	}

	private static final class DirectOntologyLoader extends BaseOntologyLoader {

		private DirectOntologyLoader(IRIMapper iriMapper, OntologyCleaner cleaner) {
			super(iriMapper, cleaner);
		}

		OWLGraphWrapper load(ConfiguredOntology ontology)
				throws OWLOntologyCreationException, IOException
		{
			return getResource(ontology);
		}
	}
}
