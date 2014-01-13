package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.ScmHelper;

public class CommitSvnUserKeyFileModule extends AbstractOboCommitSvnModule {

	private final String svnUsername;
	private final File svnKeyFile;
	private final boolean obo;
	private final boolean owl;

	private CommitSvnUserKeyFileModule(boolean obo,
			boolean owl,
			String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
		this.obo = obo;
		this.owl = owl;
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
	protected void bindScmHelper() {
		if (obo) {
			bind(ScmHelper.class, SvnHelper.OboSvnHelperKeyFile.class);
		}
		if (owl) {
			bind(ScmHelper.class, SvnHelper.OwlSvnHelperKeyFile.class);
		}
	}
	
	public static CommitSvnUserKeyFileModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals) {
		return new CommitSvnUserKeyFileModule(true, false, svnRepository, svnOntologyFileName, svnUsername, svnKeyFile, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}
	
	public static CommitSvnUserKeyFileModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			boolean svnLoadExternals) {
		return new CommitSvnUserKeyFileModule(false, true, svnRepository, svnOntologyFileName, svnUsername, svnKeyFile, applicationProperties, null, svnLoadExternals);
	}
}
