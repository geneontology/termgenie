package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.ScmHelper;

public class CommitSvnAnonymousModule extends AbstractOboCommitSvnModule {

	private final boolean obo;
	private final boolean owl;

	private CommitSvnAnonymousModule(boolean obo,
			boolean owl,
			String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
		this.obo = obo;
		this.owl = owl;
	}

	@Override
	protected void bindScmHelper() {
		if (obo) {
			bind(ScmHelper.class, SvnHelper.OboSvnHelperAnonymous.class);
		}
		if (owl) {
			bind(ScmHelper.class, SvnHelper.OwlSvnHelperAnonymous.class);
		}
	}

	public static CommitSvnAnonymousModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		return new CommitSvnAnonymousModule(true, false, svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}
	
	public static CommitSvnAnonymousModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		return new CommitSvnAnonymousModule(false, true, svnRepository, svnOntologyFileName, applicationProperties, null, svnLoadExternals);
	}
}
