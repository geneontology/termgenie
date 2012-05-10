package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOboTools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
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
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.OWLAxiom;

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

		final MixingNameProvider nameProvider;
		
		File scmFolder = null;
		File oboFolder = null;
		File oboRoundTripFolder = null;

		List<File> scmTargetOntologies = null;
		List<File> targetOntologies = null;
		List<File> modifiedTargetOntologies = null;
		List<File> modifiedSCMTargetOntologies = null;

		/**
		 * @param nameProvider
		 */
		public OboCommitData(NameProvider nameProvider) {
			super();
			this.nameProvider = new MixingNameProvider(nameProvider);
		}

		@Override
		public List<File> getSCMTargetFiles() {
			return scmTargetOntologies;
		}

		@Override
		public List<File> getTargetFiles() {
			return targetOntologies;
		}

		@Override
		public List<File> getModifiedTargetFiles() {
			return modifiedTargetOntologies;
		}

		@Override
		public List<File> getModifiedSCMTargetFiles() {
			return modifiedSCMTargetOntologies;
		}

		/**
		 * @return the scmFolder
		 */
		public File getScmFolder() {
			return scmFolder;
		}

		
		static class MixingNameProvider implements NameProvider {

			private final NameProvider mainProvider;
			private Map<String, String> otherNames = new HashMap<String, String>();

			MixingNameProvider(NameProvider mainProvider) {
				this.mainProvider = mainProvider;
			}

			@Override
			public String getName(String id) {
				String name = mainProvider.getName(id);
				if (name != null) {
					return name;
				}
				return otherNames.get(id);
			}

			@Override
			public String getDefaultOboNamespace() {
				return mainProvider.getDefaultOboNamespace();
			}
			
			public void addName(String id, String name) {
				otherNames.put(id, name);
			}
			
		}
	}

	public OboCommitData prepareWorkflow(File workFolder, NameProvider nameProvider) throws CommitException {
		OboCommitData data = new OboCommitData(nameProvider);

		data.scmFolder = createFolder(workFolder, "scm");
		data.oboFolder = createFolder(workFolder, "obo");
		data.oboRoundTripFolder = createFolder(workFolder, "obo-roundtrip");
		final File patchedFolder = createFolder(workFolder, "obo-patched");
		data.modifiedSCMTargetOntologies =  new ArrayList<File>(targetOntologyFileNames.size());
		for(String name : targetOntologyFileNames) {
			data.modifiedSCMTargetOntologies.add(new File(patchedFolder, name));
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
		data.scmTargetOntologies = new ArrayList<File>(targetOntologyFileNames.size());
		for(String name : targetOntologyFileNames) {
			data.scmTargetOntologies.add(new File(data.scmFolder, name));
		}

		// load ontology
		return loadOntologies(data.scmTargetOntologies);
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

	public void checkTargetOntologies(OboCommitData data, List<OBODoc> targetOntologies)
			throws CommitException
	{
		data.targetOntologies = new ArrayList<File>();
		for (int i = 0; i < targetOntologies.size(); i++) {
			// round trip ontology
			// This step is required to create a minimal patch.
			final String fileName = targetOntologyFileNames.get(i);
			File oboFile = createOBOFile(data.oboRoundTripFolder, fileName, targetOntologies.get(i), data.nameProvider);
			data.targetOntologies.add(oboFile);
			// check that the round trip leads to major changes
			// This is a requirement for applying the diff to the original scm file
			boolean noMajorChanges = compareRoundTripFile(data.scmTargetOntologies.get(i), oboFile);
			if (noMajorChanges == false) {
				String message = "Can write ontology for commit. Too many format changes cannot create diff: "+fileName;
				throw error(message, true);
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
		boolean success = true;
		if (terms != null && !terms.isEmpty()) {
			for (CommitedOntologyTerm term : terms) {
				Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
				List<Pair<Frame, Set<OWLAxiom>>> changes = CommitHistoryTools.translateSimple(term.getChanged());
				boolean csuccess = handleTerm(frame,
						changes,
						term.getOperation(),
						oboDoc);
				if (data != null && data.nameProvider != null) {
					data.nameProvider.addName(frame.getId(), term.getLabel());
				}
				success = success && csuccess;
			}
		}
		return success;
	}

	public void createModifiedTargetFiles(OboCommitData data, List<OBODoc> ontologies, String savedBy)
			throws CommitException
	{
		int ontologyCount = ontologies.size();
		data.modifiedTargetOntologies = new ArrayList<File>(ontologyCount);
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
			File oboFile = createOBOFile(data.oboFolder, targetOntologyFileNames.get(i), ontology, data.nameProvider);
			data.modifiedTargetOntologies.add(oboFile);
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
	 * @param data
	 * @param diff
	 * @param state
	 * @throws CommitException
	 */
	public void commitToRepository(String commitMessage,
			VersionControlAdapter scm,
			OboCommitData data,
			String diff,
			ProcessState state) throws CommitException
	{
		final List<File> modifiedSCMTargetFiles = data.getModifiedSCMTargetFiles();
		final List<File> scmTargetFiles = data.getSCMTargetFiles();
		for (int i = 0; i < modifiedSCMTargetFiles.size(); i++) {
			copyFileForCommit(modifiedSCMTargetFiles.get(i), scmTargetFiles.get(i));
		}

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

	private boolean compareRoundTripFile(File scmFile, File roundtripOboFile)
			throws CommitException
	{
		int sourceCount = countLines(scmFile);
		int roundTripCount = countLines(roundtripOboFile);

		// check that the round trip does not modify
		// the overall structure of the document
		int lineDiffCount = Math.abs(sourceCount - roundTripCount);
		Logger.getLogger(getClass()).info("Line diffs: "+lineDiffCount+ " Original: "+sourceCount+" RoundTrip: "+roundTripCount);
		return lineDiffCount == 0;
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

	private File createOBOFile(File oboFolder, String name, OBODoc oboDoc, NameProvider nameProvider) throws CommitException {
		BufferedWriter bufferedWriter = null;
		try {
			// write OBO file to temp
			OBOFormatWriter writer = new OBOFormatWriter();

			File oboFile = new File(oboFolder, name);
			oboFile.getParentFile().mkdirs();
			bufferedWriter = new BufferedWriter(new FileWriter(oboFile));
			writer.write(oboDoc, bufferedWriter, nameProvider);
			return oboFile;
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
		}
		return ontologies;
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
		return OntologyCommitReviewPipeline.error(message, exception, rollback, getClass());
	}

	protected CommitException error(String message, boolean rollback) {
		return OntologyCommitReviewPipeline.error(message, rollback, getClass());
	}
}
