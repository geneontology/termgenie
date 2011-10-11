package org.bbop.termgenie.ontology.go;

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
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyCommitPipeline;
import org.bbop.termgenie.ontology.OntologyCommitPipeline.OntologyCommitPipelineData;
import org.bbop.termgenie.ontology.go.AbstractOntologyCommitAdapter.OboCommitData;
import org.bbop.termgenie.ontology.impl.BaseOntologyLoader;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * Main steps for committing ontology changes to an OBO file in an CVS
 * repository.
 */
abstract class AbstractOntologyCommitAdapter extends OntologyCommitPipeline<CVSTools, OboCommitData, OBODoc>
{

	private final DirectOntologyLoader loader;
	private final String cvsOntologyFileName;

	AbstractOntologyCommitAdapter(ConfiguredOntology source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			String cvsOntologyFileName,
			CommitHistoryStore store,
			boolean supportAnonymus)
	{
		super(source, store, supportAnonymus);
		this.cvsOntologyFileName = cvsOntologyFileName;
		loader = new DirectOntologyLoader(iriMapper, cleaner);
	}

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

	@Override
	protected OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		OboCommitData data = new OboCommitData();

		data.cvsFolder = createFolder(workFolder, "cvs");
		data.oboFolder = createFolder(workFolder, "obo");
		data.oboRoundTripFolder = createFolder(workFolder, "obo-roundtrip");
		final File patchedFolder = createFolder(workFolder, "obo-patched");
		data.modifiedSCMTargetOntology = new File(patchedFolder, source.getUniqueName() + ".obo");

		return data;
	}

	@Override
	protected CVSTools prepareSCM(CommitInfo commitInfo, OboCommitData data) throws CommitException
	{
		final CVSTools cvs = createCVS(commitInfo, data.cvsFolder);
		return cvs;
	}

	protected abstract CVSTools createCVS(CommitInfo commitInfo, File cvsFolder);

	@Override
	protected OBODoc retrieveTargetOntology(CVSTools cvs, OboCommitData data)
			throws CommitException
	{
		// check-out ontology from cvs repository
		cvsCheckout(cvs);
		data.scmTargetOntology = new File(data.cvsFolder, cvsOntologyFileName);
		
		// load ontology
		return loadOntology(data.scmTargetOntology);
	}

	@Override
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

	@Override
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

	@Override
	protected void createModifiedTargetFile(OboCommitData data, OBODoc ontology)
			throws CommitException
	{
		// write changed ontology to a file
		data.modifiedTargetOntology = createOBOFile(data.oboFolder, ontology);
	}

	@Override
	protected abstract void commitToRepository(CommitInfo commitInfo,
			CVSTools scm,
			OboCommitData data,
			String diff) throws CommitException;

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

	private static final class DirectOntologyLoader extends BaseOntologyLoader {

		private DirectOntologyLoader(IRIMapper iriMapper, OntologyCleaner cleaner) {
			super(iriMapper, cleaner);
		}

		OBODoc loadOBO(File file, String ontology) throws IOException {
			return loadOBO(ontology, file.toURI().toURL());
		}
	}
}
