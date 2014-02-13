package org.bbop.termgenie.ontology.cvs;

import java.io.File;

import org.bbop.termgenie.cvs.CvsTools;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public final class CvsHelperPassword extends OboScmHelper {

	private final String cvsPassword;
	private final String cvsRoot;

	@Inject
	CvsHelperPassword(IRIMapper iriMapper,
			@Named("CommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
			@Named("CommitAdapterCVSPassword") String cvsPassword,
			@Named("CommitAdapterCVSRoot") String cvsRoot)
	{
		super(iriMapper, cvsOntologyFileName);
		this.cvsPassword = cvsPassword;
		this.cvsRoot = cvsRoot;
	}

	@Override
	public VersionControlAdapter createSCM(File cvsFolder)
	{
		CvsTools cvs = new CvsTools(cvsRoot, cvsPassword, cvsFolder);
		return cvs;
	}
}