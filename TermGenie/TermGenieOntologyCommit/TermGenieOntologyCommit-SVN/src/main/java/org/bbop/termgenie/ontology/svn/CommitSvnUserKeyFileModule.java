package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserKeyFileModule extends AbstractCommitSvnModule {

	private final String svnUsername;
	private final File svnKeyFile;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param svnKeyFile
	 * @param applicationProperties
	 * @param commitTargetOntologyName
	 * @param additionalOntologyFileNames 
	 * @param svnLoadExternals
	 */
	public CommitSvnUserKeyFileModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			String commitTargetOntologyName,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName, additionalOntologyFileNames, svnLoadExternals);
		this.svnUsername = svnUsername;
		this.svnKeyFile = svnKeyFile;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNUsername", svnUsername);
		bind("CommitAdapterSVNKeyFile", svnKeyFile);
		bindSecret("CommitAdapterSVNPassword");
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperKeyFile.class);
	}
}
