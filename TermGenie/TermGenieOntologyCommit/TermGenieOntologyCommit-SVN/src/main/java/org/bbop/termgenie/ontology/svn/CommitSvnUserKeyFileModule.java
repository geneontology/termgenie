package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserKeyFileModule extends AbstractCommitSvnModule {

	private final String svnUsername;
	private final File svnKeyFile;
	private final String svnPassword;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param svnKeyFile
	 * @param svnPassword
	 * @param applicationProperties
	 * @param commitTargetOntologyName
	 */
	public CommitSvnUserKeyFileModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			String svnPassword,
			Properties applicationProperties,
			String commitTargetOntologyName)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, commitTargetOntologyName);
		this.svnUsername = svnUsername;
		this.svnKeyFile = svnKeyFile;
		this.svnPassword = svnPassword;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterSVNUsername", svnUsername);
		bind("GeneOntologyCommitAdapterSVNKeyFile", svnKeyFile);
		bind("GeneOntologyCommitAdapterSVNPassword", svnPassword);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, SvnHelper.SvnHelperKeyFile.class);
	}
}
