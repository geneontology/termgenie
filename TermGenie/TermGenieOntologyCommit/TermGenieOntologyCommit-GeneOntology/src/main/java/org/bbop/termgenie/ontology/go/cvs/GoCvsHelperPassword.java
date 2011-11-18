package org.bbop.termgenie.ontology.go.cvs;

import java.io.File;

import org.bbop.termgenie.cvs.CvsTools;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public final class GoCvsHelperPassword extends OboScmHelper {

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
	public VersionControlAdapter createSCM(CommitMode commitMode,
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
		CvsTools cvs = new CvsTools(cvsRoot, realPassword, cvsFolder);
		return cvs;
	}

	@Override
	public boolean isSupportAnonymus() {
		return false;
	}

	@Override
	public CommitMode getCommitMode() {
		return CommitMode.explicit;
	}

	@Override
	public String getCommitUserName() {
		return null; // encoded in the cvs root
	}

	@Override
	public String getCommitPassword() {
		return cvsPassword;
	}
}