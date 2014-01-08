package org.bbop.termgenie.ontology.cvs;

import java.io.File;

import org.bbop.termgenie.cvs.CvsTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public final class CvsHelperAnonymous extends OboScmHelper {

	private final String cvsRoot;

	@Inject
	CvsHelperAnonymous(IRIMapper iriMapper,
			@Named("CommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
			@Named("CommitAdapterCVSRoot") String cvsRoot)
	{
		super(iriMapper, cvsOntologyFileName, null);
		this.cvsRoot = cvsRoot;
	}

	@Override
	public VersionControlAdapter createSCM(File cvsFolder) throws CommitException
	{
		return new CvsTools(cvsRoot, null, cvsFolder);
	}

}