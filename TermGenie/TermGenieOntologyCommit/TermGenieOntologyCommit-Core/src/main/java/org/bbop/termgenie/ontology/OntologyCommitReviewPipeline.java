package org.bbop.termgenie.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;

import owltools.graph.OWLGraphWrapper;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * Main steps for directly committing ontology changes to an ontology file in an
 * SCM repository.
 * 
 * @param <WORKFLOWDATA> the data during the commit process
 * @param <ONTOLOGY> the type of target ontology
 */
public abstract class OntologyCommitReviewPipeline<WORKFLOWDATA extends OntologyCommitPipelineData, ONTOLOGY> implements
		OntologyCommitReviewPipelineStages.AfterReview,
		OntologyCommitReviewPipelineStages.BeforeReview,
		Committer
{
	private static final Logger logger = Logger.getLogger(OntologyCommitReviewPipeline.class);
	

	protected final OntologyTaskManager source;
	private final CommitHistoryStore store;
	private final TermFilter<ONTOLOGY> termFilter;
	private final ReviewMailHandler handler;
	private final boolean supportAnonymus;

	protected OntologyCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			TermFilter<ONTOLOGY> termFilter,
			ReviewMailHandler handler,
			boolean supportAnonymus)
	{
		super();
		this.source = source;
		this.termFilter = termFilter;
		this.handler = handler;
		this.supportAnonymus = supportAnonymus;
		this.store = store;
	}

	@Override
	public CommitResult commit(CommitInfo commitInfo) throws CommitException {
		try {
			// add terms to local commit log
			Date date = new Date();
			CommitHistoryItem historyItem = CommitHistoryTools.create(commitInfo.getTerms(), commitInfo.getCommitMessage(), commitInfo.getUserData(), date);
			store.add(historyItem, source.getOntology().getUniqueName());
			String diff = createDiff(historyItem, source);
			return new CommitResult(true, "Your commit has been stored and awaits review by the ontology editors.", commitInfo.getTerms(), diff);
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
	public List<CommitResult> commit(List<Integer> historyIds, ProcessState state) throws CommitException
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
				throw new CommitException("Anonymus mode is not supported for the commit.", true);
			}
			return commitInternal(historyIds, mode, username, password, state);
		}
		return Collections.singletonList(CommitResult.ERROR);
	}

	@Override
	public List<Pair<String, String>> checkRecentCommits(List<String> labels) {
		try {
			return store.checkRecentCommits(source.getOntology().getUniqueName(), labels);
		} catch (CommitHistoryStoreException exception) {
			Logger.getLogger(getClass()).error("Could not check for existing term labels due to db error.", exception);
			return Collections.emptyList();
		}
	}

	@Override
	public CommitHistoryItem getItem(int itemId) throws CommitException {
		try {
			List<CommitHistoryItem> items = store.load(Collections.singletonList(Integer.valueOf(itemId)));
			if (items.size() == 1) {
				return items.get(0);
			}
			return null;
		} catch (CommitHistoryStoreException exception) {
			throw new CommitException("Could not retrieve item with id: '"+itemId+"' from store", exception, false);
		}
	}

	@Override
	public void updateItem(CommitHistoryItem item) throws CommitException {
		try {
			store.update(item, source.getOntology().getUniqueName());
		} catch (CommitHistoryStoreException exception) {
			throw new CommitException("Could not update item in db", exception, false);
		}
		
	}

	protected abstract CommitMode getCommitMode();
	
	protected abstract String getCommitUserName();
	
	protected abstract String getCommitPassword();
	
	private List<CommitResult> commitInternal(List<Integer> historyIds,
			CommitMode mode,
			String username,
			String password,
			ProcessState state) throws CommitException
	{
		// setup temporary work folder
		final WorkFolders workFolders = createTempDir();

		try {
			return commitInternal(historyIds, mode, username, password, workFolders, state);
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
			final WorkFolders workFolders, 
			final ProcessState state) throws CommitException
	{
		ProcessState.addMessage(state, "Preparing to commit "+historyIds.size()+" items.");
		CommittingNameProviderTask task = new CommittingNameProviderTask(historyIds, mode, username, password, workFolders, handler, state);
		source.runManagedTask(task);
		if (task.exception != null) {
			throw task.exception;
		}
		ProcessState.addMessage(state, "Done committing changes.");
		return task.results;
	}

	private class CommittingNameProviderTask implements ManagedTask<OWLGraphWrapper> {

		private final List<Integer> historyIds;
		private final CommitMode mode;
		private final String username;
		private final String password;
		private final WorkFolders workFolders;
		private final ProcessState state;
		private final ReviewMailHandler handler;
		private List<CommitResult> results;
		private CommitException exception;

		CommittingNameProviderTask(List<Integer> historyIds,
				CommitMode mode,
				String username,
				String password,
				final WorkFolders workFolders,
				ReviewMailHandler handler,
				final ProcessState state)
		{
			this.historyIds = historyIds;
			this.mode = mode;
			this.username = username;
			this.password = password;
			this.workFolders = workFolders;
			this.handler = handler;
			this.state = state;
		}
		
		@Override
		public Modified run(OWLGraphWrapper graph) {
			try {
				ProcessState.addMessage(state, "Preparing to commit "+historyIds.size()+" items.");
				WORKFLOWDATA data = prepareWorkflow(workFolders.workFolder, new OwlGraphWrapperNameProvider(graph));

				VersionControlAdapter scm = prepareSCM(mode, username, password, data);

				ProcessState.addMessage(state, "Preparing target ontology.");
				List<ONTOLOGY> targetOntologies = retrieveTargetOntologies(scm, data, state);
				// check for valid ontology file
				final List<File> scmTargetFiles = data.getSCMTargetFiles();
				if (scmTargetFiles == null || scmTargetFiles.isEmpty()) {
					throw error("scm target file is null");
				}

				List<CommitHistoryItem> items = retrieveItems(historyIds);
				results = new ArrayList<Committer.CommitResult>(items.size());

				boolean changed = false;
				
				logger.info("Start - Commiting, count: "+items.size());
				
				for (CommitHistoryItem item : items) {
					if (item.isCommitted()) {
						results.add(new CommitResult(false, "The item has already been marked as committed", null, null));
						continue;
					}
					CommitResult result = handleItem(item, state, targetOntologies, scm, data);
					if (result != null) {
						results.add(result);
						changed = true;
						if (result.isSuccess()) {
							handler.handleReviewMail(item);
						}
					}
				}
				logger.info("Finished - Commiting, count: "+items.size());
				// Reload ontology after committing the changes
				if (changed) {
					ProcessState.addMessage(state, "Start reloading ontology.");
					return Modified.update;
				}
			} catch (CommitException exception) {
				this.exception = exception;
			}
			return Modified.no;
		}
		
	}
	
	protected CommitResult handleItem(CommitHistoryItem item,
			ProcessState state,
			List<ONTOLOGY> targetOntologies,
			VersionControlAdapter scm,
			WORKFLOWDATA data) throws CommitException
	{
		ProcessState.addMessage(state, "Updating target ontology from repository.");
		updateSCM(scm, targetOntologies, data, state);

		checkTargetOntology(data, targetOntologies);

		ProcessState.addMessage(state, "Apply changes for commit item '"+item.getId()+"' to ontology.");
		
		final int ontologyCount = targetOntologies.size();
		
		List<File> targetFiles = data.getTargetFiles();
		assertFiles(targetFiles, ontologyCount, "targetFiles");
		List<File> scmTargetFiles = data.getSCMTargetFiles();
		assertFiles(scmTargetFiles, ontologyCount, "scmTargetFiles");
		
		List<File> modifiedSCMTargetFiles = data.getModifiedSCMTargetFiles();
		assertFiles(modifiedSCMTargetFiles, ontologyCount, "modifiedSCMTargetFiles");
		
		StringBuilder diffBuilder = new StringBuilder();
		for (int i = 0; i < ontologyCount; i++) {
			ONTOLOGY targetOntology = targetOntologies.get(i);

			List<CommitedOntologyTerm> changes;
			if (ontologyCount > 1) {
				changes = termFilter.filterTerms(item, targetOntology, targetOntologies, i);
			}
			else {
				changes = item.getTerms();
			}
			
			if (changes != null && !changes.isEmpty()) {
				// apply changes to ontology in memory
				final boolean success = applyChanges(changes, targetOntology);
				if (!success) {
					String message = "Could not apply changes to ontology.";
					throw new CommitException(message, true);
				}
			}
		}
		updateNameProvider(data, targetOntologies);
		
		// write changed ontology to a file
		createModifiedTargetFiles(data, targetOntologies, item.getSavedBy());
		
		List<File> modifiedTargetFiles = data.getModifiedTargetFiles();
		assertFiles(modifiedTargetFiles, ontologyCount, "modifiedTargetFiles");
		
		for (int i = 0; i < ontologyCount; i++) {	
			// check for valid ontology files
			File targetFile = targetFiles.get(i);
			if (targetFile == null) {
				throw error("target file is null");
			}
			File scmTargetFile = scmTargetFiles.get(i);
			if (scmTargetFile == null) {
				throw error("scm target file is null");
			}
			File modifiedTargetFile = modifiedTargetFiles.get(i);
			if (modifiedTargetFile == null) {
				throw error("modified target file is null");
			}
			File modifiedSCMTargetFile = modifiedSCMTargetFiles.get(i);
			if (modifiedSCMTargetFile == null) {
				throw error("modified scm target file is null");
			}

			ProcessState.addMessage(state, "Creating ontology diff patch.");
			// create the diff from the written and round-trip file
			Pair<String, Patch> pair = createUnifiedDiff(targetFile,
					modifiedTargetFile,
					"original",
					"termgenie-changes");
	
			ProcessState.addMessage(state, "Create patched ontology.");
			// apply the patch to the original file, write to a new temp file
			createPatchedFile(pair.getTwo(), scmTargetFile, modifiedSCMTargetFile);
			
			// append diff to buffer
			if (diffBuilder.length() > 0) {
				diffBuilder.append("\n\n");
				diffBuilder.append(pair.getOne());
			}

		}
		// commit the changes to the repository
		ProcessState.addMessage(state, "Attempting to commit for item: "+item.getId());
		String diff = diffBuilder.toString();
		commitToRepository(item.getCommitMessage(), scm, data, diff, state);
		ProcessState.addMessage(state, "Successfull commit of patch", diff);

		// set the commit also to success in the commit history
		ProcessState.addMessage(state, "Updating internal database.");
		finalizeCommitHistory(item);

		return new CommitResult(true, null, CommitHistoryTools.translate(item), diff);
	}
	
	private void assertFiles(List<File> files, int length, String name) throws CommitException {
		if (files == null) {
			throw error(name+" file list is null");
		}
		if (files.isEmpty()) {
			throw error(name+" file list is empty");
		}
		if (files.size() != length) {
			throw error(name+" file list has unexpected length, expected: "+length+" but was: "+files.size());
		}
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
	 * @param nameProvider
	 * @return WORKFLOWDATA
	 * @throws CommitException
	 */
	protected abstract WORKFLOWDATA prepareWorkflow(File workFolder, NameProvider nameProvider) throws CommitException;

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
	protected abstract VersionControlAdapter prepareSCM(CommitMode mode,
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
	protected abstract void updateSCM(VersionControlAdapter scm, List<ONTOLOGY> targetOntologies, WORKFLOWDATA data, ProcessState state)
			throws CommitException;

	/**
	 * Retrieve the target ontology and load it into memory.
	 * 
	 * @param data
	 * @return ONTOLOGY
	 * @throws CommitException
	 */
	protected abstract List<ONTOLOGY> retrieveTargetOntologies(VersionControlAdapter scm, WORKFLOWDATA data, ProcessState state)
			throws CommitException;

	/**
	 * Perform checks and metrics on the loaded ontology. Throw an
	 * {@link CommitException} in case of errors.
	 * 
	 * @param data
	 * @throws CommitException
	 */
	protected abstract void checkTargetOntology(WORKFLOWDATA data, List<ONTOLOGY> targetOntologies)
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
	 * Update the name provider with the given set of ontologies. Is called
	 * after {@link #applyChanges(List, Object)}.
	 * 
	 * @param data
	 * @param targetOntologies
	 */
	protected abstract void updateNameProvider(WORKFLOWDATA data, List<ONTOLOGY> targetOntologies);
	
	/**
	 * Write the ontology to file.
	 * 
	 * @param data
	 * @param ontologies
	 * @param savedBy
	 * @throws CommitException
	 */
	protected abstract void createModifiedTargetFiles(WORKFLOWDATA data, List<ONTOLOGY> ontologies, String savedBy)
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
			VersionControlAdapter scm,
			WORKFLOWDATA data,
			String diff, 
			ProcessState state) throws CommitException;

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

	private void createPatchedFile(Patch patch, File scmTargetFile, File modifiedSCMTargetFile) throws CommitException {
		try {
			List<String> originalLines = FileUtils.readLines(scmTargetFile);
			List<?> patched = DiffUtils.patch(originalLines, patch);
			FileUtils.writeLines(modifiedSCMTargetFile, patched);
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
		return error(message, exception, false, getClass());
	}

	protected CommitException error(String message) {
		return error(message, false, getClass());
	}
	
	public static CommitException error(String message, Throwable exception, boolean rollback, Class<?> cls) {
		Logger.getLogger(cls).warn(message, exception);
		return new CommitException(message, exception, rollback);
	}

	public static CommitException error(String message, boolean rollback, Class<?> cls) {
		Logger.getLogger(cls).warn(message);
		return new CommitException(message, rollback);
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
