package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOboTools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCommitPipelineData;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipeline;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.impl.BaseOntologyLoader;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper.OboAndOwlNameProvider;

/**
 * Main steps for committing ontology changes to an OBO file in an SCM
 * repository.
 */
public abstract class OboScmHelper {

	private final DirectOntologyLoader loader;
	private final List<String> targetOntologyFileNames;

	protected OboScmHelper(IRIMapper iriMapper,
			OntologyCleaner cleaner,
			String svnOntologyFileName,
			List<String> svnAdditionalOntologyFileNames)
	{
		this.targetOntologyFileNames = new ArrayList<String>(1);
		targetOntologyFileNames.add(svnOntologyFileName);
		if (svnAdditionalOntologyFileNames != null) {
			targetOntologyFileNames.addAll(svnAdditionalOntologyFileNames);
		}
		loader = new DirectOntologyLoader(iriMapper, cleaner);
	}

	public abstract boolean isSupportAnonymus();

	public abstract CommitMode getCommitMode();

	public abstract String getCommitUserName();

	public abstract String getCommitPassword();

	public static class OboCommitData implements OntologyCommitPipelineData {

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

	public OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		final OboCommitData data = new OboCommitData();

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

	public abstract VersionControlAdapter createSCM(CommitMode commitMode,
			String username,
			String password,
			File scmFolder) throws CommitException;

	public List<OBODoc> retrieveTargetOntologies(VersionControlAdapter scm, OboCommitData data, ProcessState state)
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
	 * @param oboDoc
	 * @return true, if changes have been apply successfully
	 * @throws CommitException
	 */
	public boolean applyHistoryChanges(OboCommitData data, List<CommitedOntologyTerm> terms, OBODoc oboDoc)
			throws CommitException
	{
		try {
			boolean success = true;
			if (terms != null && !terms.isEmpty()) {
				for (CommitedOntologyTerm term : terms) {
					Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
					List<Pair<Frame, Set<OWLAxiom>>> changes = CommitHistoryTools.translateSimple(term.getChanged());
					boolean csuccess = handleTerm(frame,
							changes,
							term.getOperation(),
							oboDoc);
					success = success && csuccess;
				}
			}
			return success;
		} catch (OBOFormatParserException exception) {
			throw new CommitException("Could not apply change to history, due to an internal error.", exception, false);
		}
	}

	public void createModifiedTargetFiles(OboCommitData data, List<OBODoc> ontologies, OWLGraphWrapper graph, String savedBy)
			throws CommitException
	{
		int ontologyCount = ontologies.size();
		for (int i = 0; i < ontologyCount; i++) {
			// write changed ontology to a file
			final OBODoc ontology = ontologies.get(i);
			Frame headerFrame = ontology.getHeaderFrame();
			if (headerFrame != null) {
				// set date
				updateClause(headerFrame, OboFormatTag.TAG_DATE, new Date());
				
				// set saved-by
				updateClause(headerFrame, OboFormatTag.TAG_SAVED_BY, savedBy);
				
				// set auto-generated-by
				updateClause(headerFrame, OboFormatTag.TAG_AUTO_GENERATED_BY, "TermGenie 1.0");
			}
			createOBOFile(data.patchFileList.get(i), ontology, new OboAndOwlNameProvider(ontology, graph));
		}
	}
	
	private void updateClause(Frame frame, OboFormatTag tag, Object value) {
		Clause clause = frame.getClause(tag);
		if (clause == null) {
			clause = new Clause(tag);
			frame.addClause(clause);
		}
		clause.setValue(value);
	}

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

	private void createOBOFile(File oboFile, OBODoc oboDoc, NameProvider nameProvider) throws CommitException {
		BufferedWriter bufferedWriter = null;
		try {
			// write OBO file to temp
			OBOFormatWriter writer = new OBOFormatWriter();
			oboFile.getParentFile().mkdirs();
			bufferedWriter = new BufferedWriter(new FileWriter(oboFile));
			writer.write(oboDoc, bufferedWriter, nameProvider);
		} catch (IOException exception) {
			String message = "Could not write ontology changes to file";
			throw error(message, exception, true);
		}
		finally {
			IOUtils.closeQuietly(bufferedWriter);
		}
	}

	private List<OBODoc> loadOntologies(List<File> scmFiles) throws CommitException {
		List<OBODoc> ontologies = new ArrayList<OBODoc>(scmFiles.size());
		try {
			// load OBO
			for(File scmFile : scmFiles) {
				OBODoc ontology = loader.loadOBO(scmFile, null);
				ontologies.add(ontology);
			}
		} catch (IOException exception) {
			String message = "Could not load recent copy of the ontology";
			throw error(message, exception, true);
		} catch (OBOFormatParserException exception) {
			String message = "Could not load recent copy of the ontology, due to an OBO format parse exception.";
			throw error(message, exception, true);
		}
		return ontologies;
	}

	private static final class DirectOntologyLoader extends BaseOntologyLoader {

		private DirectOntologyLoader(IRIMapper iriMapper, OntologyCleaner cleaner) {
			super(iriMapper, cleaner);
		}

		OBODoc loadOBO(File file, String ontology) throws IOException, OBOFormatParserException {
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
		return OntologyCommitReviewPipeline.error(message, exception, rollback, getClass());
	}

	protected CommitException error(String message, boolean rollback) {
		return OntologyCommitReviewPipeline.error(message, rollback, getClass());
	}
}
