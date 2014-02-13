package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitSvnAnonymousModule {

	private static class CommitOboSvnAnonymousModule extends AbstractOboCommitSvnModule {
	
		private CommitOboSvnAnonymousModule(String svnRepository,
				String svnOntologyFileName,
				Properties applicationProperties,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
		}
		
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					SvnHelper.OboSvnHelperAnonymous.class);
		}
	}

	private static class CommitOwlSvnAnonymousModule extends AbstractOwlCommitSvnModule {
		
		private CommitOwlSvnAnonymousModule(String svnRepository,
				String svnOntologyFileName,
				Properties applicationProperties,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
		}
		
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					SvnHelper.OwlSvnHelperAnonymous.class);
		}
	}
	

	/**
	 * Create an commit module for OBO and SVN without authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		return new CommitOboSvnAnonymousModule(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
	}
	
	/**
	 * Create an commit module for OWL and SVN without authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		return new CommitOwlSvnAnonymousModule(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
	}
}
