package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserPasswdModule extends AbstractCommitSvnModule {

	private final String svnUsername;
	private final String svnPassword;

	public CommitSvnUserPasswdModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			String svnPassword,
			Properties applicationProperties,
			String commitTargetOntologyName)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName);
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
		bind(OboScmHelper.class, SvnHelper.SvnHelperPassword.class);
	}
}
