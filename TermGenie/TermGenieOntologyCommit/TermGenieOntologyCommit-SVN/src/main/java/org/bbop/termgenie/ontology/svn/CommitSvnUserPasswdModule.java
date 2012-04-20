package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserPasswdModule extends AbstractCommitSvnModule {

	private final String svnUsername;

	public CommitSvnUserPasswdModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			String commitTargetOntologyName,
			List<String> additionalOntologyFileNames)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName, additionalOntologyFileNames);
		this.svnUsername = svnUsername;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNUsername", svnUsername);
		bindSecret("CommitAdapterSVNPassword");
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperPassword.class);
	}
}
