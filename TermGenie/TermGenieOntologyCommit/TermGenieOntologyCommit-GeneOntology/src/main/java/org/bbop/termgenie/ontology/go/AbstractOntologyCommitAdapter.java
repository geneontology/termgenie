package org.bbop.termgenie.ontology.go;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
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
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

import difflib.DiffUtils;
import difflib.Patch;

abstract class AbstractOntologyCommitAdapter implements Committer {

	private final ConfiguredOntology source;
	private final DirectOntologyLoader loader;
	private final String cvsOntologyFileName;
	private final CommitHistoryStore store;
	private final boolean supportAnonymus;

	AbstractOntologyCommitAdapter(ConfiguredOntology source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			String cvsOntologyFileName,
			CommitHistoryStore store,
			boolean supportAnonymus)
	{
		super();
		this.source = source;
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.supportAnonymus = supportAnonymus;
		loader = new DirectOntologyLoader(iriMapper, cleaner);
		this.store = store;
	}

	@Override
	public CommitResult commit(CommitInfo commitInfo) throws CommitException {
		List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms = commitInfo.getTerms();
		if (terms != null && !terms.isEmpty()) {
			if (commitInfo.getCommitMode() == CommitMode.explicit) {
				if (commitInfo.getUsername() == null) {
					throw new CommitException("If explicit mode is selected, an username is required.", true);
				}
				if (commitInfo.getPassword() == null) {
					throw new CommitException("If explicit mode is selected, a password is required.", true);
				}
			}
			else if (commitInfo.getCommitMode() == CommitMode.anonymus && !supportAnonymus) {
				throw new CommitException("Anonymus mode is not supported for the GeneOntology commit.", true);
			}
			return commitInternal(commitInfo);
		}
		return CommitResult.ERROR;
	}

	private CommitResult commitInternal(CommitInfo commitInfo) throws CommitException {
		List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms = commitInfo.getTerms();
		List<CommitObject<Relation>> relations = commitInfo.getRelations();

		final WorkFolders workFolders = createTempDir();

		try {
			return commitInternal(commitInfo, terms, relations, workFolders);
		}
		finally {
			workFolders.clean();
		}
	}

	static class WorkFolders {

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
		
		void clean() {
			FileUtils.deleteQuietly(workFolder);
			FileUtils.deleteQuietly(lockFile);
		}
	}

	WorkFolders createTempDir() throws CommitException {
		File tempFile = null;
		File workFolder = null;
		try {
			String suffix = ".lock";
			tempFile = File.createTempFile(source.getUniqueName() + "commit-", suffix);
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
			throw error("Could not create temporary work dir.", exception, true);
		}
	}

	private CommitResult commitInternal(CommitInfo commitInfo,
			List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations,
			final WorkFolders workFolders) throws CommitException
	{
		final File cvsFolder = createCVSFolder(workFolders);
		final CVSTools cvs = createCVS(commitInfo, cvsFolder);
		final File oboFolder = createOBOFolder(workFolders);

		cvsCheckout(cvs);

		final File cvsFile = new File(cvsFolder, cvsOntologyFileName);
		OWLGraphWrapper ontology = loadOntology(cvsFile);

		final OBODoc oboDoc = createOBODoc(ontology);
		applyChanges(terms, relations, oboDoc);

		final File oboFile = createOBOFile(oboFolder, oboDoc);

		final CommitHistoryData commitHistoryData = updateCommitHistory(commitInfo,
				terms,
				relations,
				ontology);

		// get diff from the two files
		String cvsDiff = createUnifiedDiff(cvsFile,
				oboFile,
				cvsOntologyFileName,
				"termgenie-changes");

		commitToRepository(commitInfo, cvs, cvsFile, oboFile, cvsDiff);

		finalizeCommitHistory(ontology, commitHistoryData);
		return new CommitResult(true, cvsDiff);
	}

	protected File createCVSFolder(final WorkFolders workFolders) throws CommitException {
		final File cvsFolder;
		try {
			cvsFolder = new File(workFolders.workFolder, "cvs");
			FileUtils.forceMkdir(cvsFolder);
		} catch (IOException exception) {
			String message = "Could not create working directories for the commit";
			throw error(message, exception, true);
		}
		return cvsFolder;
	}

	protected File createOBOFolder(final WorkFolders workFolders) throws CommitException {
		final File oboFolder;
		try {
			oboFolder = new File(workFolders.workFolder, "obo");
			FileUtils.forceMkdir(oboFolder);
		} catch (IOException exception) {
			String message = "Could not create working directories for the commit";
			throw error(message, exception, true);
		}
		return oboFolder;
	}

	protected void cvsCheckout(CVSTools cvs) throws CommitException {
		try {
			// cvs checkout
			cvs.connect();
			cvs.checkout(cvsOntologyFileName);
		} catch (IOException exception) {
			String message = "Could not checkout recent copy of the ontology";
			throw error(message, exception, true);
		}
		finally {
			try {
				cvs.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close CVS tool.", exception);
			}
		}
	}

	protected void applyChanges(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations,
			final OBODoc oboDoc)
	{
		if (terms != null && !terms.isEmpty()) {
			for (CommitObject<OntologyTerm<Synonym, IRelation>> commitObject : terms) {
				ComitAwareOBOConverterTools.handleTerm(commitObject.getObject(),
						commitObject.getType(),
						oboDoc);
			}
		}
		if (relations != null && !relations.isEmpty()) {
			for (CommitObject<Relation> commitObject : relations) {
				ComitAwareOBOConverterTools.handleRelation(commitObject.getObject(),
						commitObject.getType(),
						oboDoc);
			}
		}
	}

	protected CommitHistoryData updateCommitHistory(CommitInfo commitInfo,
			List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations,
			OWLGraphWrapper ontology) throws CommitException
	{
		final CommitHistoryData commitHistoryData;
		try {
			// add terms to local commit log
			CommitHistory history = store.loadAll(source.getUniqueName());
			Date date = new Date();
			String user = commitInfo.getUsername();
			CommitHistoryItem historyItem;
			if (history == null) {
				history = CommitHistoryTools.create(terms, relations, user, date);
				historyItem = history.getItems().get(0);
			}
			else {
				historyItem = CommitHistoryTools.add(history, terms, relations, user, date);
			}
			store.store(history);
			commitHistoryData = new CommitHistoryData(history, historyItem);
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + ontology;
			throw error(message, exception, true);
		}
		return commitHistoryData;
	}

	private static class CommitHistoryData {
	
		CommitHistory history;
		CommitHistoryItem historyItem;
	
		CommitHistoryData(CommitHistory history, CommitHistoryItem historyItem) {
			this.history = history;
			this.historyItem = historyItem;
		}
	}

	protected abstract void commitToRepository(CommitInfo commitInfo,
			CVSTools cvs,
			File cvsFile,
			File oboFile,
			String cvsDiff) throws CommitException;

	private String createUnifiedDiff(File originalFile,
			File revisedFile,
			String originalName,
			String revisedName) throws CommitException
	{
		Logger.getLogger(getClass()).info("Create Diff");
		try {
			List<String> originalLines = FileUtils.readLines(originalFile);
			List<String> revisedLines = FileUtils.readLines(revisedFile);
			Patch patch = DiffUtils.diff(originalLines, revisedLines);
			if (patch != null) {
				List<String> diff = DiffUtils.generateUnifiedDiff(originalName,
						revisedName,
						originalLines,
						patch,
						0);
				StringBuilder sb = new StringBuilder();
				for (String line : diff) {
					sb.append(line).append('\n');
				}
				return sb.toString();
			}
		} catch (IOException exception) {
			throw error("Could not create diff for commit.", exception, true);
		}
		return null;
	}

	protected void finalizeCommitHistory(OWLGraphWrapper ontology,
			CommitHistoryData commitHistoryData) throws CommitException
	{
		try {
			// set terms in commit log as committed
			commitHistoryData.historyItem.setCommitted(true);
			store.store(commitHistoryData.history);
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + ontology;
			throw error(message, exception, false);
		}
	}

	protected void copyOBOFileForCommit(File cvsTarget, File source) throws CommitException {
		try {
			// copy changed ontology over cvs checkout
			FileUtils.copyFile(source, cvsTarget);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to commit file";
			throw new CommitException(message, exception, true);
		}
	}

	private OBODoc createOBODoc(OWLGraphWrapper ontology) throws CommitException {
		try {
			// write OBO file to temp
			Owl2Obo converter = new Owl2Obo();
			OBODoc oboDoc = converter.convert(ontology.getSourceOntology());
			return oboDoc;
		} catch (OWLOntologyCreationException exception) {
			String message = "Could not convert ontology to OBO";
			throw error(message, exception, true);
		}
	}

	private File createOBOFile(File oboFolder, OBODoc oboDoc) throws CommitException {
		BufferedWriter bufferedWriter = null;
		try {
			// write OBO file to temp
			OBOFormatWriter writer = new OBOFormatWriter();

			File oboFile = new File(oboFolder, source.getUniqueName() + ".obo");
			bufferedWriter = new BufferedWriter(new FileWriter(oboFile));
			writer.write(oboDoc, bufferedWriter);
			return oboFile;			
		} catch (IOException exception) {
			String message = "Could not write ontology changes to file";
			throw error(message, exception, true);
		}
		finally {
			IOUtils.closeQuietly(bufferedWriter);
		}
	}

	private OWLGraphWrapper loadOntology(File cvsFile) throws CommitException {
		OWLGraphWrapper ontology;
		try {
			// load OBO
			String localSource = cvsFile.getAbsoluteFile().toURI().toURL().toString();
			ConfiguredOntology config = ConfiguredOntology.createCopy(source, localSource);
			ontology = loader.load(config);
		} catch (OWLOntologyCreationException exception) {
			String message = "Could load recent copy of the ontology";
			throw error(message, exception, true);
		} catch (IOException exception) {
			String message = "Could load recent copy of the ontology";
			throw error(message, exception, true);
		}
		return ontology;
	}

	protected abstract CVSTools createCVS(CommitInfo commitInfo, File cvsFolder);

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

	protected CommitException error(String message, Throwable exception, boolean rollback) {
		Logger.getLogger(getClass()).warn(message, exception);
		return new CommitException(message, exception, rollback);
	}
}
