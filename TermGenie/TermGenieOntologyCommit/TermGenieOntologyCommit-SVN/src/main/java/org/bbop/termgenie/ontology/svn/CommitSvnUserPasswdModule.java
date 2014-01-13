package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class CommitSvnUserPasswdModule extends AbstractOboCommitSvnModule {

	private final String svnUsername;
	private final boolean obo;
	private final boolean owl;

	private CommitSvnUserPasswdModule(boolean obo,
			boolean owl,
			String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
		this.obo = obo;
		this.owl = owl;
		this.svnUsername = svnUsername;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNUsername", svnUsername);
		bindSecret("CommitAdapterSVNPassword");
	}

	@Override
	protected void bindScmHelper() {
		if (obo) {
			bind(OboScmHelper.class, SvnHelper.OboSvnHelperPassword.class);
		}
		if (owl) {
			bind(ScmHelper.class, SvnHelper.OwlSvnHelperPassword.class);
		}
	}
	
	public static CommitSvnUserPasswdModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals) {
		return new CommitSvnUserPasswdModule(true, false, svnRepository, svnOntologyFileName, svnUsername, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}
	
	public static CommitSvnUserPasswdModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals) {
		return new CommitSvnUserPasswdModule(false, true, svnRepository, svnOntologyFileName, svnUsername, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}
}
