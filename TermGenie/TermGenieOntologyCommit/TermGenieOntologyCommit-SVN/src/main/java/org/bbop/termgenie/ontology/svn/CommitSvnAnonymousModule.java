package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnAnonymousModule extends AbstractCommitSvnModule {

	public CommitSvnAnonymousModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			String commitTargetOntologyName,
			List<String> additionalOntologyFileNames)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName, additionalOntologyFileNames);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperAnonymous.class);

	}

}
