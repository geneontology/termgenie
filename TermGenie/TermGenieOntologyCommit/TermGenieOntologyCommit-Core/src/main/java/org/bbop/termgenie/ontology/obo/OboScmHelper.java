package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOboTools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCommitPipeline;
import org.bbop.termgenie.ontology.OntologyCommitPipelineData;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.impl.BaseOntologyLoader;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools.LoadState;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

/**
 * Main steps for committing ontology changes to an OBO file in an SCM
 * repository.
 */
public abstract class OboScmHelper {

	private final OntologyTaskManager source;
	private final DirectOntologyLoader loader;
	private final String targetOntologyFileName;

	protected OboScmHelper(OntologyTaskManager source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			String targetOntologyFileName)
	{
		this.source = source;
		this.targetOntologyFileName = targetOntologyFileName;
		loader = new DirectOntologyLoader(iriMapper, cleaner);
	}

	public abstract boolean isSupportAnonymus();

	public abstract CommitMode getCommitMode();

	public abstract String getCommitUserName();

	public abstract String getCommitPassword();

	public static class OboCommitData implements OntologyCommitPipelineData {

		File scmFolder = null;
		File oboFolder = null;
		File oboRoundTripFolder = null;

		File scmTargetOntology = null;
		File targetOntology = null;
		File modifiedTargetOntology = null;
		File modifiedSCMTargetOntology = null;

		@Override
		public File getSCMTargetFile() {
			return scmTargetOntology;
		}

		@Override
		public File getTargetFile() {
			return targetOntology;
		}

		@Override
		public File getModifiedTargetFile() {
			return modifiedTargetOntology;
		}

		@Override
		public File getModifiedSCMTargetFile() {
			return modifiedSCMTargetOntology;
		}

		/**
		 * @return the scmFolder
		 */
		public File getScmFolder() {
			return scmFolder;
		}

	}

	public OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		OboCommitData data = new OboCommitData();

		data.scmFolder = createFolder(workFolder, "scm");
		data.oboFolder = createFolder(workFolder, "obo");
		data.oboRoundTripFolder = createFolder(workFolder, "obo-roundtrip");
		final File patchedFolder = createFolder(workFolder, "obo-patched");
		data.modifiedSCMTargetOntology = new File(patchedFolder, source.getOntology().getUniqueName() + ".obo");

		return data;
	}

	public VersionControlAdapter prepareSCM(CommitInfo commitInfo, OboCommitData data)
			throws CommitException
	{
		final VersionControlAdapter scm = createSCM(commitInfo.getCommitMode(),
				commitInfo.getUsername(),
				commitInfo.getPassword(),
				data.scmFolder);
		return scm;
	}

	public abstract VersionControlAdapter createSCM(CommitMode commitMode,
			String username,
			String password,
			File scmFolder) throws CommitException;

	public OBODoc retrieveTargetOntology(VersionControlAdapter scm, OboCommitData data)
			throws CommitException
	{
		// check-out ontology from SCM repository
		scmCheckout(scm);
		data.scmTargetOntology = new File(data.scmFolder, targetOntologyFileName);

		// load ontology
		return loadOntology(data.scmTargetOntology);
	}
	
	public void updateSCM(VersionControlAdapter scm)
			throws CommitException
	{
		try {
			scm.connect();
			scm.update(targetOntologyFileName);
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

	public void checkTargetOntology(OboCommitData data, OBODoc targetOntology)
			throws CommitException
	{

		// round trip ontology
		// This step is required to create a minimal patch.
		data.targetOntology = createOBOFile(data.oboRoundTripFolder, targetOntology);
		// check that the round trip leads to major changes
		// This is a requirement for applying the diff to the original scm file
		boolean noMajorChanges = compareRoundTripFile(data.scmTargetOntology, data.targetOntology);
		if (noMajorChanges == false) {
			String message = "Can write ontology for commit. Too many format changes cannot create diff.";
			throw error(message, true);
		}
	}

	public boolean applyChanges(List<CommitObject<TermCommit>> terms, final OBODoc oboDoc) {
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitObject<TermCommit> commitObject : terms) {
				boolean csuccess = LoadState.isSuccess(handleTerm(commitObject.getObject(),
						commitObject.getType(),
						oboDoc));
				success = success && csuccess;
			}
		}
		return success;
	}

	/**
	 * @param terms
	 * @param oboDoc
	 * @return true, if changes have been apply successfully
	 * @throws CommitException
	 */
	public boolean applyHistoryChanges(List<CommitedOntologyTerm> terms, OBODoc oboDoc)
			throws CommitException
	{
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitedOntologyTerm term : terms) {
				Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
				List<Frame> changes = CommitHistoryTools.translateSimple(term.getChanged());
				boolean csuccess = LoadState.isSuccess(handleTerm(frame,
						changes,
						term.getOperation(),
						oboDoc));
				success = success && csuccess;
			}
		}
		return success;
	}

	public void createModifiedTargetFile(OboCommitData data, OBODoc ontology)
			throws CommitException
	{
		// write changed ontology to a file
		data.modifiedTargetOntology = createOBOFile(data.oboFolder, ontology);
	}

	/**
	 * @param commitMessage
	 * @param scm
	 * @param data
	 * @param diff
	 * @throws CommitException
	 */
	public void commitToRepository(String commitMessage,
			VersionControlAdapter scm,
			OboCommitData data,
			String diff) throws CommitException
	{
		copyFileForCommit(data.getModifiedSCMTargetFile(), data.getSCMTargetFile());

		try {
			scm.connect();
			scm.commit(commitMessage, targetOntologyFileName);
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

	private boolean compareRoundTripFile(File scmFile, File roundtripOboFile)
			throws CommitException
	{
		int sourceCount = countLines(scmFile);
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

	private void scmCheckout(VersionControlAdapter cvs) throws CommitException {
		try {
			// cvs checkout
			cvs.connect();
			cvs.checkout(targetOntologyFileName);
		} catch (IOException exception) {
			String message = "Could not checkout recent copy of the ontology";
			throw error(message, exception, true);
		}
		finally {
			try {
				cvs.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close SCM tool.", exception);
			}
		}
	}

	private File createOBOFile(File oboFolder, OBODoc oboDoc) throws CommitException {
		BufferedWriter bufferedWriter = null;
		try {
			// write OBO file to temp
			OBOFormatWriter writer = new OBOFormatWriter();

			File oboFile = new File(oboFolder, source.getOntology().getUniqueName() + ".obo");
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

	private OBODoc loadOntology(File scmFile) throws CommitException {
		OBODoc ontology;
		try {
			// load OBO
			ontology = loader.loadOBO(scmFile, null);
		} catch (IOException exception) {
			String message = "Could load recent copy of the ontology";
			throw error(message, exception, true);
		}
		return ontology;
	}

	private static final class DirectOntologyLoader extends BaseOntologyLoader {

		private DirectOntologyLoader(IRIMapper iriMapper, OntologyCleaner cleaner) {
			super(iriMapper, cleaner);
		}

		OBODoc loadOBO(File file, String ontology) throws IOException {
			return loadOBO(ontology, file.toURI().toURL());
		}
	}

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
		return OntologyCommitPipeline.error(message, exception, rollback, getClass());
	}

	protected CommitException error(String message, boolean rollback) {
		return OntologyCommitPipeline.error(message, rollback, getClass());
	}
}
