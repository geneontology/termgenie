package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserPasswdModule extends AbstractCommitSvnModule {

	private final String svnUsername;

	public CommitSvnUserPasswdModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			String commitTargetOntologyName)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName);
		this.svnUsername = svnUsername;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterSVNUsername", svnUsername);
		bindSecret("GeneOntologyCommitAdapterSVNPassword");
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperPassword.class);
	}
}
