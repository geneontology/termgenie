package org.bbop.termgenie.ontology.go;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
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
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

import owltools.graph.OWLGraphWrapper.Synonym;

import difflib.DiffUtils;
import difflib.Patch;

/**
 * Main steps for committing ontology changes to an OBO file in an CVS
 * repository.
 */
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

		// setup temporary work folder
		final WorkFolders workFolders = createTempDir();

		try {
			return commitInternal(commitInfo, terms, relations, workFolders);
		}
		finally {
			// delete temp folder
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
		// create sub folders in work directory
		final File cvsFolder = createCVSFolder(workFolders);
		final CVSTools cvs = createCVS(commitInfo, cvsFolder);
		final File oboFolder = createOBOFolder(workFolders);

		// check-out ontology from cvs repository
		cvsCheckout(cvs);

		final File cvsFile = new File(cvsFolder, cvsOntologyFileName);

		// load ontology
		final OBODoc oboDoc = loadOntology(cvsFile);

		// round trip ontology
		// This step is required to create a minimal patch.
		final File roundtripOboFile = roundtripObo(workFolders, oboDoc);

		// check that the round trip leads to major changes
		// This is a requirement for applying the diff to the original cvs file
		boolean noMajorChanges = compareRoundTripFile(cvsFile, roundtripOboFile);
		if (noMajorChanges == false) {
			String message = "Can write ontology for commit. Too many format changes cannot create diff.";
			throw new CommitException(message, true);
		}

		// apply changes to ontology in memory
		final boolean success = applyChanges(terms, relations, oboDoc);
		if (!success) {
			String message = "Could not apply changes to ontology.";
			throw new CommitException(message, true);
		}

		// write changed ontology to a file
		final File oboFile = createOBOFile(oboFolder, oboDoc);

		// store the changes in the local commit history,
		// mark them as unfinished
		final CommitHistoryData commitHistoryData = updateCommitHistory(commitInfo,
				terms,
				relations);

		// create the diff from the written and round-trip file
		Pair<String, Patch> pair = createUnifiedDiff(roundtripOboFile,
				oboFile,
				cvsOntologyFileName,
				"termgenie-changes");

		// apply the patch to the original file, write to a new temp file
		File patchedFile = createPatchedFile(workFolders, cvsFile, pair.getTwo());

		// commit the changes to the repository
		String diff = pair.getOne();
		commitToRepository(commitInfo, cvs, cvsFile, patchedFile, diff);

		// set the commit also to success in the commit history
		finalizeCommitHistory(commitHistoryData);
		return new CommitResult(true, diff);
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

	private File roundtripObo(final WorkFolders workFolders, OBODoc oboDoc) throws CommitException {
		File roundTripFolder = new File(workFolders.workFolder, "obo-roundtrip");
		try {
			FileUtils.forceMkdir(roundTripFolder);
			File roundTripFile = createOBOFile(roundTripFolder, oboDoc);
			return roundTripFile;
		} catch (IOException exception) {
			String message = "Could not create working directoriy for the commit: " + roundTripFolder;
			throw error(message, exception, true);
		}
	}

	private boolean compareRoundTripFile(File cvsFile, File roundtripOboFile)
			throws CommitException
	{
		int sourceCount = countLines(cvsFile);
		int roundTripCount = countLines(roundtripOboFile);

		// check that the round trip does not modify
		// the overall structure of the document
		return Math.abs(sourceCount - roundTripCount) <= 1;
	}

	private int countLines(File file) throws CommitException {
		try {
			LineIterator iterator = FileUtils.lineIterator(file);
			int count = 0;
			while (iterator.hasNext()) {
				iterator.next();
				count += 1;
			}
			return count;
		} catch (IOException exception) {
			String message = "Could not create read file during commit: " + file.getAbsolutePath();
			throw error(message, exception, true);
		}
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

	protected boolean applyChanges(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations,
			final OBODoc oboDoc)
	{
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitObject<OntologyTerm<Synonym, IRelation>> commitObject : terms) {
				boolean csuccess = ComitAwareOBOConverterTools.handleTerm(commitObject.getObject(),
						commitObject.getType(),
						oboDoc);
				success = success && csuccess;
			}
		}
		if (relations != null && !relations.isEmpty()) {
			for (CommitObject<Relation> commitObject : relations) {
				boolean csuccess = ComitAwareOBOConverterTools.handleRelation(commitObject.getObject(),
						commitObject.getType(),
						oboDoc);
				success = success && csuccess;
			}
		}
		return success;
	}

	protected CommitHistoryData updateCommitHistory(CommitInfo commitInfo,
			List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations) throws CommitException
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
			String message = "Problems handling commit history for ontology: " + source.getUniqueName();
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

	private Pair<String, Patch> createUnifiedDiff(File originalFile,
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
				return new Pair<String, Patch>(sb.toString(), patch);
			}
		} catch (IOException exception) {
			throw error("Could not create diff for commit.", exception, true);
		}
		return null;
	}

	private File createPatchedFile(WorkFolders workFolders, File cvsFile, Patch patch)
			throws CommitException
	{
		File patchedFolder = new File(workFolders.workFolder, "obo-patched");
		try {
			FileUtils.forceMkdir(patchedFolder);
			List<String> originalLines = FileUtils.readLines(cvsFile);
			List<?> patched = DiffUtils.patch(originalLines, patch);
			File patchedFile = new File(patchedFolder, source.getUniqueName() + ".obo");
			FileUtils.writeLines(patchedFile, patched);
			return patchedFile;
		} catch (Exception exception) {
			String message = "Could not create patched file for commit";
			throw error(message, exception, true);
		}
	}

	protected void finalizeCommitHistory(CommitHistoryData commitHistoryData)
			throws CommitException
	{
		try {
			// set terms in commit log as committed
			commitHistoryData.historyItem.setCommitted(true);
			store.store(commitHistoryData.history);
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + source.getUniqueName();
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

	private OBODoc loadOntology(File cvsFile) throws CommitException {
		OBODoc ontology;
		try {
			// load OBO
			ontology = loader.loadOBO(cvsFile, source.getUniqueName());
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

		OBODoc loadOBO(File file, String ontology) throws IOException {
			return loadOBO(ontology, file.toURI().toURL());
		}
	}

	protected CommitException error(String message, Throwable exception, boolean rollback) {
		Logger.getLogger(getClass()).warn(message, exception);
		return new CommitException(message, exception, rollback);
	}
}
