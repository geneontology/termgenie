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

import difflib.DiffUtils;
import difflib.Patch;

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
	public CommitResult commit(CommitInfo commitInfo) throws CommitException {
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
		return CommitResult.ERROR;
	}

	private CommitResult commitInternal(CommitInfo commitInfo) throws CommitException {
		List<CommitObject<OntologyTerm>> terms = commitInfo.getTerms();
		List<CommitObject<Relation>> relations = commitInfo.getRelations();

		CVSTools cvs = null;
		
		WorkFolders workFolders = createTempDir();  
		try {
			File cvsFolder;
			try {
				cvsFolder = new File(workFolders.workFolder, "cvs");
				FileUtils.forceMkdir(cvsFolder);
			} catch (IOException exception) {
				String message = "Could not create working directories for the commit";
				throw new CommitException(message, exception, true);
			}
			
			cvs = createCVS(commitInfo, cvsFolder);
			
			File oboFolder;
			try {
				oboFolder = new File(workFolders.workFolder, "obo");
				FileUtils.forceMkdir(oboFolder);
			} catch (IOException exception) {
				String message = "Could not create working directories for the commit";
				throw new CommitException(message, exception, true);
			}
			
			try {
				// cvs checkout
				cvs.connect();
				cvs.checkout(cvsOntologyFileName);
			} catch (IOException exception) {
				String message = "Could not checkout recent copy of the ontology";
				throw new CommitException(message, exception, true);
			}
			finally {
				try {
					cvs.close();
				} catch (IOException exception) {
					logger.error("Could not close CVS tool.", exception);
				}
			}
			
			File cvsGoFile = new File(cvsFolder, cvsOntologyFileName);
			OWLGraphWrapper ontology = loadOntology(cvsGoFile);
			
			// TODO apply changes

			// reason and check validity
			// only throw error if there is an inconsistency,
			// which can not be recovered silently!
			ReasonerTaskManager reasonerManager = reasonerFactory.getDefaultTaskManager(ontology);
			
			// TODO do reasoning over created ontology to find errors and inconsistencies
			
			File oboFile = createOBOFile(oboFolder, ontology);
			
			CommitHistory history;
			CommitHistoryItem historyItem;
			try {
				// add terms to local commit log
				history = store.loadAll(go.getUniqueName());
				Date date = new Date();
				String user = commitInfo.getUsername();
				if (history == null) {
					history = CommitHistoryTools.create(terms, relations, user, date, go);
					historyItem = history.getItems().get(0);
				}
				else {
					historyItem = CommitHistoryTools.add(history, terms, relations, user, date, go);
				}
				store.store(history);
			} catch (CommitHistoryStoreException exception) {
				String message = "Problems handling commit history for ontology: "+ontology;
				throw new CommitException(message, exception, true);
			}
			
			// get diff from the two files
			String cvsDiff = createUnifiedDiff(cvsGoFile, oboFile, cvsOntologyFileName, "termgenie-changes");
			copyOBOFileForCommit(cvsGoFile, oboFile);

			try {
				// commit cvs changes
				cvs.connect();
				cvs.commit("TermGenie commit for user: " + commitInfo.getTermgenieUser());
			} catch (IOException exception) {
				throw new CommitException("Error during CVS commit", exception, false);
			}
			finally {
				try {
					cvs.close();
				} catch (IOException exception) {
					logger.error("Could not close CVS tool.", exception);
				}
			}

			try {
				// set terms in commit log as committed
				historyItem.setCommitted(true);
				store.store(history);
			} catch (CommitHistoryStoreException exception) {
				String message = "Problems handling commit history for ontology: "+ontology;
				throw new CommitException(message, exception, false);
			}
			return new CommitResult(true, cvsDiff);
		}
		finally {
			FileUtils.deleteQuietly(workFolders.workFolder);
			FileUtils.deleteQuietly(workFolders.lockFile);
		}
	}

	private String createUnifiedDiff(File originalFile, File revisedFile, String originalName, String revisedName) throws CommitException {
		try {
			List<String> originalLines = FileUtils.readLines(originalFile);
			List<String> revisedLines = FileUtils.readLines(revisedFile);
			Patch patch = DiffUtils.diff(originalLines, revisedLines);
			if (patch != null) {
				List<String> diff = DiffUtils.generateUnifiedDiff(originalName, revisedName, originalLines, patch, 0);
				StringBuilder sb = new StringBuilder();
				for (String line : diff) {
					sb.append(line).append('\n');
				}
				return sb.toString();
			}
		} catch (IOException exception) {
			throw new CommitException("Could not create diff for commit.", exception, true);
		}
		return null;
	}

	private WorkFolders createTempDir() throws CommitException {
		File tempFile = null;
		File workFolder = null;
		try {
			String suffix = ".lock";
			tempFile = File.createTempFile("gocommit-", suffix);
			String tempFolderName = tempFile.getName().replace(suffix, "-folder");
			workFolder = new File(tempFile.getParentFile(), tempFolderName);
			FileUtils.forceMkdir(workFolder);
			return new WorkFolders(tempFile, workFolder);
		} catch (Exception exception) {
			if (tempFile != null) {
				FileUtils.deleteQuietly(tempFile);
			}
			if (workFolder != null) {
				FileUtils.deleteQuietly(workFolder);
			}
			throw new CommitException("Could not create temporary work dir.", exception, true);
		}
	}
	
	private static class WorkFolders {
		
		private final File lockFile;
		private final File workFolder;
		
		/**
		 * @param lockFile
		 * @param workFolder
		 */
		WorkFolders(File lockFile, File workFolder) {
			super();
			this.lockFile = lockFile;
			this.workFolder = workFolder;
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
