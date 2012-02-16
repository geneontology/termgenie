package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnAnonymousModule extends AbstractCommitSvnModule {

	public CommitSvnAnonymousModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			String commitTargetOntologyName)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperAnonymous.class);

	}

}
