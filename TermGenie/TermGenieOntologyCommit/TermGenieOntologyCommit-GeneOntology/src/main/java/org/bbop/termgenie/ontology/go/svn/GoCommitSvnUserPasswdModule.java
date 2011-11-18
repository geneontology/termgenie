package org.bbop.termgenie.ontology.go.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class GoCommitSvnUserPasswdModule extends AbstractGoCommitSvnModule {

	private final String svnUsername;
	private final String svnPassword;

	public GoCommitSvnUserPasswdModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			String svnPassword,
			Properties applicationProperties)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties);
		this.svnUsername = svnUsername;
		this.svnPassword = svnPassword;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterSVNUsername", svnUsername);
		bind("GeneOntologyCommitAdapterSVNPassword", svnPassword);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class).to(GoSvnHelper.GoSvnHelperPassword.class);
	}
}
