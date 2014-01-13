package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.TermFilter;
import org.obolibrary.oboformat.model.OBODoc;

public class CommitSvnAnonymousModule {

	private static class CommitOboSvnAnonymousModule extends AbstractOboCommitSvnModule {
	
		private CommitOboSvnAnonymousModule(String svnRepository,
				String svnOntologyFileName,
				Properties applicationProperties,
				List<String> additionalOntologyFileNames,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
		}
		
		@Override
		protected void bindScmHelper() {
			bind(ScmHelper.class, SvnHelper.OboSvnHelperAnonymous.class);
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
			bind(ScmHelper.class, SvnHelper.OwlSvnHelperAnonymous.class);
		}
	}
	

	/**
	 * Create an commit module for OBO and SVN without authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		return new CommitOboSvnAnonymousModule(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}
	
	/**
	 * Create an commit module for OBO, OBOTermFilter and SVN without authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 * @param filter
	 * @return module
	 */
	public static IOCModule createFilteredOboModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals,
			final TermFilter<OBODoc> filter)
	{
		return new CommitOboSvnAnonymousModule(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals) {

			@Override
			protected TermFilter<OBODoc> provideTermFilter() {
				return filter;
			}
		};
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
