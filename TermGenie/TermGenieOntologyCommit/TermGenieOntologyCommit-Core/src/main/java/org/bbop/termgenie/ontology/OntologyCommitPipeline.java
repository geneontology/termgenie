package org.bbop.termgenie.ontology;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.tools.Pair;

import owltools.graph.OWLGraphWrapper.ISynonym;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * Main steps for directly committing ontology changes to an ontology file in an SCM
 * repository.
 * 
 * @param <SCM> the tool to access and modify the ontology repository
 * @param <WORKFLOWDATA> the data during the commit process
 * @param <ONTOLOGY> the type of target ontology
 */
public abstract class OntologyCommitPipeline<SCM, WORKFLOWDATA extends OntologyCommitPipelineData, ONTOLOGY> implements
		Committer
{
	protected final OntologyTaskManager source;
	private final CommitHistoryStore store;
	private final boolean supportAnonymus;

	protected OntologyCommitPipeline(OntologyTaskManager source,
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
		List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms = commitInfo.getTerms();
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
		List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms = commitInfo.getTerms();

		// setup temporary work folder
		final WorkFolders workFolders = createTempDir();

		try {
			return commitInternal(commitInfo, terms, workFolders);
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
			throw error("Could not create temporary work dir.", exception, true);
		}
	}

	private CommitResult commitInternal(CommitInfo commitInfo,
			List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms,
			final WorkFolders workFolders) throws CommitException
	{
		WORKFLOWDATA data = prepareWorkflow(workFolders.workFolder);

		SCM scm = prepareSCM(commitInfo, data);

		ONTOLOGY targetOntology = retrieveTargetOntology(scm, data);
		// check for valid ontology file
		if (data.getSCMTargetFile() == null) {
			throw error("scm target file is null", true);
		}
		
		checkTargetOntology(data, targetOntology);

		// apply changes to ontology in memory
		final boolean success = applyChanges(terms, targetOntology);
		if (!success) {
			String message = "Could not apply changes to ontology.";
			throw new CommitException(message, true);
		}

		// write changed ontology to a file
		createModifiedTargetFile(data, targetOntology);

		// check for valid ontology files
		File targetFile = data.getTargetFile();
		if (targetFile == null) {
			throw error("target file is null", true);
		}
		File modifiedTargetFile = data.getModifiedTargetFile();
		if (modifiedTargetFile == null) {
			throw error("modified target file is null", true);
		}
		if (data.getModifiedSCMTargetFile() == null) {
			throw error("modified scm target file is null", true);
		}
		
		// store the changes in the local commit history,
		// mark them as unfinished
		final CommitHistoryItem item = updateCommitHistory(commitInfo, terms);

		// create the diff from the written and round-trip file
		Pair<String, Patch> pair = createUnifiedDiff(targetFile,
				modifiedTargetFile,
				"original",
				"termgenie-changes");

		// apply the patch to the original file, write to a new temp file
		createPatchedFile(data, pair.getTwo());

		// commit the changes to the repository
		String diff = pair.getOne();
		commitToRepository(commitInfo.getTermgenieUser(), scm, data, diff);

		// set the commit also to success in the commit history
		finalizeCommitHistory(item);
		
		// update ontology
		source.updateManaged();
		return new CommitResult(true, null, terms, diff);
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
	 * @param data
	 * @return SCM
	 * @throws CommitException
	 */
	protected abstract SCM prepareSCM(CommitInfo commitInfo, WORKFLOWDATA data)
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
	protected abstract boolean applyChanges(List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms,
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
	 * @param commitInfo
	 * @param scm
	 * @param data
	 * @param diff
	 * @throws CommitException
	 */
	protected abstract void commitToRepository(String username,
			SCM scm,
			WORKFLOWDATA data,
			String diff) throws CommitException;

	private CommitHistoryItem updateCommitHistory(CommitInfo commitInfo,
			List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms) throws CommitException
	{
		try {
			// add terms to local commit log
			Date date = new Date();
			String user = commitInfo.getUsername();
			CommitHistoryItem historyItem = CommitHistoryTools.create(terms, user, date);
			store.add(historyItem, source.getOntology().getUniqueName());
			return historyItem;
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + source.getOntology().getUniqueName();
			throw error(message, exception, true);
		}
	}

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

	private void createPatchedFile(WORKFLOWDATA data, Patch patch) throws CommitException {
		try {
			List<String> originalLines = FileUtils.readLines(data.getSCMTargetFile());
			List<?> patched = DiffUtils.patch(originalLines, patch);
			FileUtils.writeLines(data.getModifiedSCMTargetFile(), patched);
		} catch (Exception exception) {
			String message = "Could not create patched file for commit";
			throw error(message, exception, true);
		}
	}

	private void finalizeCommitHistory(CommitHistoryItem item) throws CommitException {
		try {
			// set terms in commit log as committed
			item.setCommitted(true);
			store.update(item, source.getOntology().getUniqueName());
		} catch (CommitHistoryStoreException exception) {
			String message = "Problems handling commit history for ontology: " + source.getOntology().getUniqueName();
			throw error(message, exception, false);
		}
	}

	protected CommitException error(String message, Throwable exception, boolean rollback) {
		return error(message, exception, rollback, getClass());
	}

	protected CommitException error(String message, boolean rollback) {
		return error(message, rollback, getClass());
	}
	
	public static CommitException error(String message, Throwable exception, boolean rollback, Class<?> cls) {
		Logger.getLogger(cls).warn(message, exception);
		return new CommitException(message, exception, rollback);
	}

	public static CommitException error(String message, boolean rollback, Class<?> cls) {
		Logger.getLogger(cls).warn(message);
		return new CommitException(message, rollback);
	}

}
