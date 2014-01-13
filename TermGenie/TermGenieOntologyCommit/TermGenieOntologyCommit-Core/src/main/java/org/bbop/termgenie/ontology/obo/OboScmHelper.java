package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOboTools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.IRIMapper;
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
import org.obolibrary.oboformat.writer.OBOFormatWriter.OBODocNameProvider;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

/**
 * Main steps for committing ontology changes to an OBO file in an SCM
 * repository.
 */
public abstract class OboScmHelper extends ScmHelper<OBODoc> {

	private final DirectOntologyLoader loader;

	protected OboScmHelper(IRIMapper iriMapper,
			String svnOntologyFileName,
			List<String> svnAdditionalOntologyFileNames)
	{
		super(svnOntologyFileName, svnAdditionalOntologyFileNames);
		loader = new DirectOntologyLoader(iriMapper);
	}

	@Override
	public abstract VersionControlAdapter createSCM(File scmFolder) throws CommitException;


	@Override
	public boolean applyHistoryChanges(ScmCommitData data, List<CommitedOntologyTerm> terms, OBODoc oboDoc)
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

	@Override
	public void createModifiedTargetFiles(ScmCommitData data, List<OBODoc> ontologies, OWLGraphWrapper graph, String savedBy)
			throws CommitException
	{
		int ontologyCount = ontologies.size();
		final NameProvider nameProvider = new MultipleOboAndOwlNameProvider(ontologies, graph);
		List<File> modifiedTargetFiles = data.getModifiedTargetFiles();
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
			createOBOFile(modifiedTargetFiles.get(i), ontology, nameProvider);
		}
	}
	
	/**
	 * Provide names for the {@link OBOFormatWriter} using an List of {@link OBODoc}
	 * first and an {@link OWLGraphWrapper} as secondary.
	 */
	public static class MultipleOboAndOwlNameProvider implements NameProvider {

		private final List<NameProvider> providers;
		private final OWLGraphWrapper graph;
		
		public MultipleOboAndOwlNameProvider(List<OBODoc> oboDocs, OWLGraphWrapper wrapper) {
			providers = new ArrayList<NameProvider>();
			for(OBODoc oboDoc : oboDocs) {
				providers.add(new OBODocNameProvider(oboDoc));
			}
			this.graph = wrapper;
		}

		@Override
		public String getName(String id) {
			String name = null;
			for(NameProvider nameProvider : providers) {
				name = nameProvider.getName(id);
				if (name != null) {
					return name;
				}
			}
			OWLObject owlObject = graph.getOWLObjectByIdentifier(id);
			if (owlObject != null) {
				name = graph.getLabel(owlObject);
			}
			return name;
		}

		@Override
		public String getDefaultOboNamespace() {
			return providers.get(0).getDefaultOboNamespace();
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

	@Override
	protected List<OBODoc> loadOntologies(List<File> scmFiles) throws CommitException {
		List<OBODoc> ontologies = new ArrayList<OBODoc>(scmFiles.size());
		try {
			// load OBO
			for(File scmFile : scmFiles) {
				OBODoc ontology = loader.loadOBO(scmFile, null, null);
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

		private DirectOntologyLoader(IRIMapper iriMapper) {
			super(iriMapper);
		}

		OBODoc loadOBO(File file, String ontology, Map<String, String> importRewrites) throws IOException, OBOFormatParserException {
			return loadOBO(ontology, file.toURI().toURL(), importRewrites);
		}
	}

}
