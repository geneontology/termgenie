package org.bbop.termgenie.ontology.go;

import java.io.File;

import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class FileOnlyGeneOntologyCommitAdapter extends AbstractOntologyCommitAdapter {

	private final File localFile;
	private final String cvsRoot;

	@Inject
	FileOnlyGeneOntologyCommitAdapter(@Named("ConfiguredOntologyGeneOntology") ConfiguredOntology source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
			@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot,
			@Named("FileOnlyGeneOntologyCommitAdapterLocalFile") String localFile,
			CommitHistoryStore store)
	{
		super(source, iriMapper, cleaner, cvsOntologyFileName, store, true);
		this.cvsRoot = cvsRoot;
		this.localFile = new File(localFile);
	}

	@Override
	protected void commitToRepository(CommitInfo commitInfo,
			CVSTools cvs,
			File cvsGoFile,
			File oboFile,
			String cvsDiff) throws CommitException
	{
		copyOBOFileForCommit(localFile, oboFile);
	}

	@Override
	protected CVSTools createCVS(CommitInfo commitInfo, File cvsFolder) {
		return new CVSTools(cvsRoot, null, cvsFolder);
	}

}
