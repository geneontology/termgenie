package org.bbop.termgenie.ontology.go.cvs;

import java.io.File;

import org.bbop.termgenie.cvs.CvsTools;
import org.bbop.termgenie.ontology.CommitException;
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
public final class GoCvsHelperAnonymous extends OboScmHelper {

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
	public VersionControlAdapter createSCM(CommitMode commitMode,
			String username,
			String password,
			File cvsFolder) throws CommitException
	{
		return new CvsTools(cvsRoot, null, cvsFolder);
	}

	@Override
	public boolean isSupportAnonymus() {
		return true;
	}

	@Override
	public CommitMode getCommitMode() {
		return CommitMode.anonymus;
	}

	@Override
	public String getCommitUserName() {
		return null; // encoded in the cvs root
	}

	@Override
	public String getCommitPassword() {
		return null; // no password
	}
}