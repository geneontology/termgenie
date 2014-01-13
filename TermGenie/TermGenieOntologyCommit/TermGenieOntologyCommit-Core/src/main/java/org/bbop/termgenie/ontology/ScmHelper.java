package org.bbop.termgenie.ontology;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.scm.VersionControlAdapter;

import owltools.graph.OWLGraphWrapper;

/**
 * Main steps for committing ontology changes to an ontology file in an SCM
 * repository.
 * @param <ONTOLOGY> 
 */
public abstract class ScmHelper<ONTOLOGY> {
	
	private static final Logger LOG = Logger.getLogger(ScmHelper.class);

	private final List<String> targetOntologyFileNames;

	protected ScmHelper(String svnOntologyFileName,
			List<String> svnAdditionalOntologyFileNames)
	{
		this.targetOntologyFileNames = new ArrayList<String>(1);
		targetOntologyFileNames.add(svnOntologyFileName);
		if (svnAdditionalOntologyFileNames != null) {
			targetOntologyFileNames.addAll(svnAdditionalOntologyFileNames);
		}
	}

	public static class ScmCommitData implements OntologyCommitPipelineData {

		File scmFolder = null;
		List<File> scmFileList = null;
		List<File> patchFileList = null;

		@Override
		public List<File> getTargetFiles() {
			return scmFileList;
		}

		@Override
		public List<File> getModifiedTargetFiles() {
			return patchFileList;
		}

		/**
		 * @return the scmFolder
		 */
		public File getScmFolder() {
			return scmFolder;
		}
	}

	public ScmCommitData prepareWorkflow(File workFolder) throws CommitException {
		final ScmCommitData data = new ScmCommitData();

		data.scmFolder = createFolder(workFolder, "scm");
		final File patchedFolder = createFolder(workFolder, "patched");
		int count = targetOntologyFileNames.size();
		data.scmFileList = new ArrayList<File>(count);
		data.patchFileList = new ArrayList<File>(count);
		for(String name : targetOntologyFileNames) {
			File scmFile = new File(data.scmFolder, name);
			File patchFile = new File(patchedFolder, name);
			data.scmFileList.add(scmFile);
			data.patchFileList.add(patchFile);
		}
		return data;
	}

	public abstract VersionControlAdapter createSCM(File scmFolder) throws CommitException;

	public List<ONTOLOGY> retrieveTargetOntologies(VersionControlAdapter scm, ScmCommitData data, ProcessState state)
			throws CommitException
	{
		// check-out ontology from SCM repository
		scmCheckout(scm, state);
		
		// load ontology
		return loadOntologies(data.scmFileList);
	}
	
	public void updateSCM(VersionControlAdapter scm, ProcessState state)
			throws CommitException
	{
		try {
			scm.connect();
			scm.update(targetOntologyFileNames, state);
		} catch (IOException exception) {
			throw error("Could not update scm repository", exception, false);
		} finally {
			try {
				scm.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close SCM tool.", exception);
			}
		}
	}

	/**
	 * @param data
	 * @param terms
	 * @param ontology
	 * @return true, if changes have been apply successfully
	 * @throws CommitException
	 */
	public abstract boolean applyHistoryChanges(ScmCommitData data, List<CommitedOntologyTerm> terms, ONTOLOGY ontology)
			throws CommitException;

	public abstract void createModifiedTargetFiles(ScmCommitData data, List<ONTOLOGY> ontologies, OWLGraphWrapper graph, String savedBy)
			throws CommitException;
	
	/**
	 * @param commitMessage
	 * @param scm
	 * @param diff
	 * @param state
	 * @throws CommitException
	 */
	public void commitToRepository(String commitMessage,
			VersionControlAdapter scm,
			String diff,
			ProcessState state) throws CommitException
	{
		try {
			scm.connect();
			scm.commit(commitMessage, targetOntologyFileNames, state);
		} catch (IOException exception) {
			throw error("Error during SCM commit", exception, false);
		}
		finally {
			try {
				scm.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close SCM tool.", exception);
			}
		}
	}

	private void scmCheckout(VersionControlAdapter scm, ProcessState state) throws CommitException {
		try {
			// scm checkout
			scm.connect();
			
			boolean success = scm.checkout(targetOntologyFileNames, state);
			if (!success) {
				String message = "Could not checkout recent copy of the ontology";
				throw error(message, true);
			}
		} catch (IOException exception) {
			String message = "Could not checkout recent copy of the ontology";
			throw error(message, exception, true);
		}
		finally {
			try {
				scm.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close SCM tool.", exception);
			}
		}
	}

	protected abstract List<ONTOLOGY> loadOntologies(List<File> scmFiles) throws CommitException;

	protected File createFolder(final File workFolder, String name) throws CommitException {
		final File folder;
		try {
			folder = new File(workFolder, name);
			FileUtils.forceMkdir(folder);
		} catch (IOException exception) {
			String message = "Could not create working directory " + name + " for the commit";
			throw error(message, exception, true);
		}
		return folder;
	}

	public void copyFileForCommit(File source, File target) throws CommitException {
		try {
			FileUtils.copyFile(source, target);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to commit file";
			throw error(message, exception, true);
		}
	}

	protected CommitException error(String message, Throwable exception, boolean rollback) {
		LOG.warn(message, exception);
		return new CommitException(message, exception, rollback);
	}

	protected CommitException error(String message, boolean rollback) {
		LOG.warn(message);
		return new CommitException(message, rollback);
	}
}
