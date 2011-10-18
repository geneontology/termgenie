package org.bbop.termgenie.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.tools.Pair;

import difflib.DiffUtils;
import difflib.Patch;

/**
 * Main steps for directly committing ontology changes to an ontology file in an
 * SCM repository.
 * 
 * @param <SCM> the tool to access and modify the ontology repository
 * @param <WORKFLOWDATA> the data during the commit process
 * @param <ONTOLOGY> the type of target ontology
 */
public abstract class OntologyCommitReviewPipeline<SCM, WORKFLOWDATA extends OntologyCommitPipelineData, ONTOLOGY> implements
		OntologyCommitReviewPipelineStages.AfterReview,
		OntologyCommitReviewPipelineStages.BeforeReview,
		Committer
{

	protected final OntologyTaskManager source;
	private final CommitHistoryStore store;
	private final boolean supportAnonymus;

	protected OntologyCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			boolean supportAnonymus)
	{
		super();
		this.source = source;
		this.supportAnonymus = supportAnonymus;
		this.store = store;
	}

	@Override
	public CommitResult commit(CommitInfo commitInfo) throws CommitException {
		try {
			// add terms to local commit log
			Date date = new Date();
			CommitHistoryItem historyItem = CommitHistoryTools.create(commitInfo.getTerms(), commitInfo.getTermgenieUser(), date);
			store.add(historyItem, source.getOntology().getUniqueName());
			String diff = createDiff(historyItem, source);
			return new CommitResult(true, "Your commit has been stored and awaits review by the ontology editors.", diff);
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + source.getOntology().getUniqueName();
			throw error(message, exception);
		}
	}

	protected abstract String createDiff(CommitHistoryItem historyItem, OntologyTaskManager source) throws CommitException;

	@Override
	public List<CommitHistoryItem> getItemsForReview() throws CommitException {
		try {
			return store.getItemsForReview(source.getOntology().getUniqueName());
		} catch (CommitHistoryStoreException exception) {
			throw error("Could not retrieve history items from db.", exception);
		}
	}

	@Override
	public List<CommitResult> commit(List<Integer> historyIds) throws CommitException
	{
		// check commit info mode
		if (historyIds != null && !historyIds.isEmpty()) {
			CommitMode mode = getCommitMode();
			String username = getCommitUserName();
			String password = getCommitPassword();
			if (mode == CommitMode.explicit) {
				if (username == null) {
					throw new CommitException("If explicit mode is selected, an username is required.", true);
				}
				if (password == null) {
					throw new CommitException("If explicit mode is selected, a password is required.", true);
				}
			}
			else if (mode == CommitMode.anonymus && !supportAnonymus) {
				throw new CommitException("Anonymus mode is not supported for the GeneOntology commit.", true);
			}
			return commitInternal(historyIds, mode, username, password);
		}
		return Collections.singletonList(CommitResult.ERROR);
	}

	protected abstract CommitMode getCommitMode();
	
	protected abstract String getCommitUserName();
	
	protected abstract String getCommitPassword();
	
	private List<CommitResult> commitInternal(List<Integer> historyIds,
			CommitMode mode,
			String username,
			String password) throws CommitException
	{
		// setup temporary work folder
		final WorkFolders workFolders = createTempDir();

		try {
			return commitInternal(historyIds, mode, username, password, workFolders);
		}
		finally {
			// delete temp folder
			workFolders.clean();
		}
	}

	protected static class WorkFolders {

		private final File lockFile;
		private final File workFolder;

		/**
		 * @param lockFile
		 * @param workFolder
		 */
		protected WorkFolders(File lockFile, File workFolder) {
			super();
			this.lockFile = lockFile;
			this.workFolder = workFolder;
		}

		protected void clean() {
			FileUtils.deleteQuietly(workFolder);
			FileUtils.deleteQuietly(lockFile);
		}
	}

	protected WorkFolders createTempDir() throws CommitException {
		File tempFile = null;
		File workFolder = null;
		try {
			String suffix = ".lock";
			tempFile = File.createTempFile(source.getOntology().getUniqueName() + "commit-", suffix);
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
			throw error("Could not create temporary work dir.", exception);
		}
	}

	private List<CommitResult> commitInternal(List<Integer> historyIds,
			CommitMode mode,
			String username,
			String password,
			final WorkFolders workFolders) throws CommitException
	{
		WORKFLOWDATA data = prepareWorkflow(workFolders.workFolder);

		SCM scm = prepareSCM(mode, username, password, data);

		ONTOLOGY targetOntology = retrieveTargetOntology(scm, data);
		// check for valid ontology file
		if (data.getSCMTargetFile() == null) {
			throw error("scm target file is null");
		}

		List<CommitHistoryItem> items = retrieveItems(historyIds);
		List<CommitResult> results = new ArrayList<Committer.CommitResult>(items.size());

		for (CommitHistoryItem item : items) {
			if (item.isCommitted()) {
				results.add(new CommitResult(false, "The item has already been marked as committed", null));
				continue;
			}
			updateSCM(scm, targetOntology, data);

			checkTargetOntology(data, targetOntology);

			// apply changes to ontology in memory
			final boolean success = applyChanges(item.getTerms(), targetOntology);
			if (!success) {
				String message = "Could not apply changes to ontology.";
				throw new CommitException(message, true);
			}

			// write changed ontology to a file
			createModifiedTargetFile(data, targetOntology);

			// check for valid ontology files
			File targetFile = data.getTargetFile();
			if (targetFile == null) {
				throw error("target file is null");
			}
			File modifiedTargetFile = data.getModifiedTargetFile();
			if (modifiedTargetFile == null) {
				throw error("modified target file is null");
			}
			if (data.getModifiedSCMTargetFile() == null) {
				throw error("modified scm target file is null");
			}

			// create the diff from the written and round-trip file
			Pair<String, Patch> pair = createUnifiedDiff(targetFile,
					modifiedTargetFile,
					"original",
					"termgenie-changes");

			// apply the patch to the original file, write to a new temp file
			createPatchedFile(data, pair.getTwo());

			// commit the changes to the repository
			String diff = pair.getOne();
			commitToRepository(item.getUser(), scm, data, diff);

			// set the commit also to success in the commit history
			finalizeCommitHistory(item);

			results.add(new CommitResult(true, null, diff));
		}
		return results;
	}

	private List<CommitHistoryItem> retrieveItems(List<Integer> historyIds) throws CommitException {
		try {
			List<CommitHistoryItem> load = store.load(historyIds);
			if (load == null || load.isEmpty()) {
				throw error("Could not retrieve commit items from db");
			}
			return load;
		} catch (CommitHistoryStoreException exception) {
			String message = "Could not retrieve commit items from db";
			throw error(message, exception);
		}
	}

	/**
	 * Prepare the work-flow and its associated data. This includes also the
	 * setup of sub folders.
	 * 
	 * @param workFolder
	 * @return WORKFLOWDATA
	 * @throws CommitException
	 */
	protected abstract WORKFLOWDATA prepareWorkflow(File workFolder) throws CommitException;

	/**
	 * Prepare the SCM module for retrieving the target ontology.
	 * 
	 * @param mode
	 * @param username
	 * @param password
	 * @param data
	 * @return SCM
	 * @throws CommitException
	 */
	protected abstract SCM prepareSCM(CommitMode mode,
			String username,
			String password,
			WORKFLOWDATA data) throws CommitException;

	/**
	 * Update the scm content from the repository
	 * 
	 * @param scm
	 * @param targetOntology
	 * @param data
	 * @throws CommitException
	 */
	protected abstract void updateSCM(SCM scm, ONTOLOGY targetOntology, WORKFLOWDATA data)
			throws CommitException;

	/**
	 * Retrieve the target ontology and load it into memory.
	 * 
	 * @param data
	 * @return ONTOLOGY
	 * @throws CommitException
	 */
	protected abstract ONTOLOGY retrieveTargetOntology(SCM scm, WORKFLOWDATA data)
			throws CommitException;

	/**
	 * Perform checks and metrics on the loaded ontology. Throw an
	 * {@link CommitException} in case of errors.
	 * 
	 * @param data
	 * @throws CommitException
	 */
	protected abstract void checkTargetOntology(WORKFLOWDATA data, ONTOLOGY targetOntology)
			throws CommitException;

	/**
	 * Apply the given changes to the ontology.
	 * 
	 * @param terms
	 * @param relations
	 * @param ontology
	 * @return true, if the changes have been applied successfully
	 * @throws CommitException
	 */
	protected abstract boolean applyChanges(List<CommitedOntologyTerm> terms,
			ONTOLOGY ontology) throws CommitException;

	/**
	 * Write the ontology to file.
	 * 
	 * @param data
	 * @param ontology
	 * @throws CommitException
	 */
	protected abstract void createModifiedTargetFile(WORKFLOWDATA data, ONTOLOGY ontology)
			throws CommitException;

	/**
	 * Execute the commit using the SCM tool.
	 * 
	 * @param username
	 * @param scm
	 * @param data
	 * @param diff
	 * @throws CommitException
	 */
	protected abstract void commitToRepository(String username,
			SCM scm,
			WORKFLOWDATA data,
			String diff) throws CommitException;

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
			throw error("Could not create diff for commit.", exception);
		}
		return null;
	}

	private void createPatchedFile(WORKFLOWDATA data, Patch patch) throws CommitException {
		try {
			List<String> originalLines = FileUtils.readLines(data.getSCMTargetFile());
			List<?> patched = DiffUtils.patch(originalLines, patch);
			FileUtils.writeLines(data.getModifiedSCMTargetFile(), patched);
		} catch (Exception exception) {
			String message = "Could not create patched file for commit";
			throw error(message, exception);
		}
	}

	private void finalizeCommitHistory(CommitHistoryItem item) throws CommitException {
		final String uniqueName = source.getOntology().getUniqueName();
		try {
			// set terms in commit log as committed
			item.setCommitted(true);
			store.update(item, uniqueName);
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + uniqueName;
			throw error(message, exception);
		}
	}

	protected CommitException error(String message, Throwable exception) {
		return OntologyCommitPipeline.error(message, exception, false, getClass());
	}

	protected CommitException error(String message) {
		return OntologyCommitPipeline.error(message, false, getClass());
	}

	protected File createFolder(final File workFolder, String name) throws CommitException {
		final File folder;
		try {
			folder = new File(workFolder, name);
			FileUtils.forceMkdir(folder);
		} catch (IOException exception) {
			String message = "Could not create working directory " + name + " for the commit";
			throw error(message, exception);
		}
		return folder;
	}

	protected void copyFileForCommit(File source, File target) throws CommitException {
		try {
			FileUtils.copyFile(source, target);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to commit file";
			throw new CommitException(message, exception, true);
		}
	}
}
