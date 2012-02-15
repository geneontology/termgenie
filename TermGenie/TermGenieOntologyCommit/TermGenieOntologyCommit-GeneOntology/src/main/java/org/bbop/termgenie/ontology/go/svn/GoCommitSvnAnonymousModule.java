package org.bbop.termgenie.ontology.go.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class GoCommitSvnAnonymousModule extends AbstractGoCommitSvnModule {

	public GoCommitSvnAnonymousModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties) {
		super(svnRepository, svnOntologyFileName, applicationProperties);
	}

	@Override
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, GoSvnHelper.GoSvnHelperAnonymous.class);
		
	}

}
