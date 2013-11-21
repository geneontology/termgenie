package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnAnonymousModule extends AbstractCommitSvnModule {

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 */
	public CommitSvnAnonymousModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperAnonymous.class);

	}

}
