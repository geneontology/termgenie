package org.bbop.termgenie.ontology.go;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GeneOntologyCommitAdapter extends AbstractOntologyCommitAdapter {

	private static final Logger logger = Logger.getLogger(GeneOntologyCommitAdapter.class);

	private final String cvsRoot;
	private final String cvsPassword;

	@Inject
	GeneOntologyCommitAdapter(@Named("ConfiguredOntologyGeneOntology") ConfiguredOntology source,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
			@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot,
			@Named("GeneOntologyCommitAdapterCVSPassword") String cvsPassword,
			CommitHistoryStore store)
	{
		super(source, iriMapper, cleaner, cvsOntologyFileName, store, false);
		this.cvsRoot = cvsRoot;
		this.cvsPassword = cvsPassword;
	}

	@Override
	protected void commitToRepository(CommitInfo commitInfo,
			CVSTools scm,
			OboCommitData data,
			String diff) throws CommitException
	{
		copyFileForCommit(data.getModifiedSCMTargetFile(), data.getSCMTargetFile());

		try {
			scm.connect();
			scm.commit("TermGenie commit for user: " + commitInfo.getTermgenieUser());
		} catch (IOException exception) {
			throw error("Error during CVS commit", exception, false);
		}
		finally {
			try {
				scm.close();
			} catch (IOException exception) {
				logger.error("Could not close CVS tool.", exception);
			}
		}
	}

	@Override
	protected CVSTools createCVS(CommitInfo commitInfo, File cvsFolder) {
		String realPassword;
		if (commitInfo.getCommitMode() == CommitMode.internal) {
			realPassword = cvsPassword;
		}
		else {
			realPassword = commitInfo.getPassword();
		}
		CVSTools cvs = new CVSTools(cvsRoot, realPassword, cvsFolder);
		return cvs;
	}

}
