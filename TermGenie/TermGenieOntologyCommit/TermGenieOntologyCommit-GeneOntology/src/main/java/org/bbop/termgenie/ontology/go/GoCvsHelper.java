package org.bbop.termgenie.ontology.go;

import static org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCommitPipeline;
import org.bbop.termgenie.ontology.OntologyCommitPipelineData;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.impl.BaseOntologyLoader;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.LoadState;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Main steps for committing ontology changes to an OBO file in an CVS
 * repository.
 */
public abstract class GoCvsHelper {

	@Singleton
	public static final class GoCvsHelperPassword extends GoCvsHelper {

		private final String cvsPassword;
		private final String cvsRoot;

		@Inject
		GoCvsHelperPassword(@Named("GeneOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
				@Named("GeneOntologyCommitAdapterCVSPassword") String cvsPassword,
				@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot)
		{
			super(source, iriMapper, cleaner, cvsOntologyFileName);
			this.cvsPassword = cvsPassword;
			this.cvsRoot = cvsRoot;
		}

		@Override
		protected CVSTools createCVS(CommitMode commitMode,
				String username,
				String password,
				File cvsFolder)
		{
			String realPassword;
			if (commitMode == CommitMode.internal) {
				realPassword = cvsPassword;
			}
			else {
				realPassword = password;
			}
			CVSTools cvs = new CVSTools(cvsRoot, realPassword, cvsFolder);
			return cvs;
		}

		@Override
		protected boolean isSupportAnonymus() {
			return false;
		}

		@Override
		protected CommitMode getCommitMode() {
			return CommitMode.explicit;
		}

		@Override
		protected String getCommitUserName() {
			return null; // encoded in the cvs root
		}

		@Override
		protected String getCommitPassword() {
			return cvsPassword;
		}
	}

	@Singleton
	public static final class GoCvsHelperAnonymous extends GoCvsHelper {

		private final String cvsRoot;

		@Inject
		GoCvsHelperAnonymous(@Named("GeneOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
				@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot)
		{
			super(source, iriMapper, cleaner, cvsOntologyFileName);
			this.cvsRoot = cvsRoot;
		}

		@Override
		protected CVSTools createCVS(CommitMode commitMode,
				String username,
				String password,
				File cvsFolder) throws CommitException
		{
			return new CVSTools(cvsRoot, null, cvsFolder);
		}

		@Override
		protected boolean isSupportAnonymus() {
			return true;
		}

		@Override
		protected CommitMode getCommitMode() {
			return CommitMode.anonymus;
		}

		@Override
		protected String getCommitUserName() {
			return null; // encoded in the cvs root
		}

		@Override
		protected String getCommitPassword() {
			return null; // no password
		}
	}

	private final OntologyTaskManager source;
	private final DirectOntologyLoader loader;
	private final String cvsOntologyFileName;

	GoCvsHelper(OntologyTaskManager source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			String cvsOntologyFileName)
	{
		this.source = source;
		this.cvsOntologyFileName = cvsOntologyFileName;
		loader = new DirectOntologyLoader(iriMapper, cleaner);
	}

	protected abstract boolean isSupportAnonymus();

	protected abstract CommitMode getCommitMode();

	protected abstract String getCommitUserName();

	protected abstract String getCommitPassword();

	static class OboCommitData implements OntologyCommitPipelineData {

		File cvsFolder = null;
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

	}

	protected OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		OboCommitData data = new OboCommitData();

		data.cvsFolder = createFolder(workFolder, "cvs");
		data.oboFolder = createFolder(workFolder, "obo");
		data.oboRoundTripFolder = createFolder(workFolder, "obo-roundtrip");
		final File patchedFolder = createFolder(workFolder, "obo-patched");
		data.modifiedSCMTargetOntology = new File(patchedFolder, source.getOntology().getUniqueName() + ".obo");

		return data;
	}

	protected CVSTools prepareSCM(CommitInfo commitInfo, OboCommitData data) throws CommitException
	{
		final CVSTools cvs = createCVS(commitInfo.getCommitMode(),
				commitInfo.getUsername(),
				commitInfo.getPassword(),
				data.cvsFolder);
		return cvs;
	}

	protected abstract CVSTools createCVS(CommitMode commitMode,
			String username,
			String password,
			File cvsFolder) throws CommitException;

	protected OBODoc retrieveTargetOntology(CVSTools cvs, OboCommitData data)
			throws CommitException
	{
		// check-out ontology from cvs repository
		cvsCheckout(cvs);
		data.scmTargetOntology = new File(data.cvsFolder, cvsOntologyFileName);

		// load ontology
		return loadOntology(data.scmTargetOntology);
	}

	protected void checkTargetOntology(OboCommitData data, OBODoc targetOntology)
			throws CommitException
	{

		// round trip ontology
		// This step is required to create a minimal patch.
		data.targetOntology = createOBOFile(data.oboRoundTripFolder, targetOntology);
		// check that the round trip leads to major changes
		// This is a requirement for applying the diff to the original cvs file
		boolean noMajorChanges = compareRoundTripFile(data.scmTargetOntology, data.targetOntology);
		if (noMajorChanges == false) {
			String message = "Can write ontology for commit. Too many format changes cannot create diff.";
			throw error(message, true);
		}
	}

	protected boolean applyChanges(List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms,
			final OBODoc oboDoc)
	{
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitObject<OntologyTerm<ISynonym, IRelation>> commitObject : terms) {
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
	protected boolean applyHistoryChanges(List<CommitedOntologyTerm> terms, OBODoc oboDoc)
			throws CommitException
	{
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitedOntologyTerm term : terms) {
				Modification mode = CommitHistoryTools.getModification(term.getOperation());
				boolean csuccess = LoadState.isSuccess(handleTerm(term, mode, oboDoc));
				success = success && csuccess;
			}
		}
		return success;
	}

	protected void createModifiedTargetFile(OboCommitData data, OBODoc ontology)
			throws CommitException
	{
		// write changed ontology to a file
		data.modifiedTargetOntology = createOBOFile(data.oboFolder, ontology);
	}

	/**
	 * @param username
	 * @param scm
	 * @param data
	 * @param diff
	 * @throws CommitException
	 */
	protected void commitToRepository(String username, CVSTools scm, OboCommitData data, String diff)
			throws CommitException
	{
		copyFileForCommit(data.getModifiedSCMTargetFile(), data.getSCMTargetFile());

		try {
			scm.connect();
			scm.commit("TermGenie commit for user: " + username);
		} catch (IOException exception) {
			throw error("Error during CVS commit", exception, false);
		}
		finally {
			try {
				scm.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close CVS tool.", exception);
			}
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

	private void cvsCheckout(CVSTools cvs) throws CommitException {
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

	private OBODoc loadOntology(File cvsFile) throws CommitException {
		OBODoc ontology;
		try {
			// load OBO
			ontology = loader.loadOBO(cvsFile, null);
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

	protected void copyFileForCommit(File source, File target) throws CommitException {
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
