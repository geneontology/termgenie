package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.TermFilter;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitSvnUserPasswdModule {

	private static class CommitOboSvnUserPasswdModule extends AbstractOboCommitSvnModule {

		private final String svnUsername;

		private CommitOboSvnUserPasswdModule(String svnRepository,
				String svnOntologyFileName,
				String svnUsername,
				Properties applicationProperties,
				List<String> additionalOntologyFileNames,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
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
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					SvnHelper.OboSvnHelperPassword.class);
		}
	}

	private static class CommitOwlSvnUserPasswdModule extends AbstractOwlCommitSvnModule {

		private final String svnUsername;

		private CommitOwlSvnUserPasswdModule(String svnRepository,
				String svnOntologyFileName,
				String svnUsername,
				Properties applicationProperties,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
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
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					SvnHelper.OwlSvnHelperPassword.class);
		}
	}

	/**
	 * Create an commit module for OBO and SVN with username and password authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		return new CommitOboSvnUserPasswdModule(svnRepository, svnOntologyFileName, svnUsername, applicationProperties, additionalOntologyFileNames, svnLoadExternals);
	}

	/**
	 * Create an commit module for OBO, OBOTermFilter and SVN with username and password authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 * @param filter
	 * @return module
	 */
	public static IOCModule createFilteredOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals,
			final TermFilter<OBODoc> filter)
	{
		return new CommitOboSvnUserPasswdModule(svnRepository, svnOntologyFileName, svnUsername, applicationProperties, additionalOntologyFileNames, svnLoadExternals)
		{

			@Override
			protected TermFilter<OBODoc> provideTermFilter() {
				return filter;
			}
		};
	}

	/**
	 * Create an commit module for OWL and SVN with username and password authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		return new CommitOwlSvnUserPasswdModule(svnRepository, svnOntologyFileName, svnUsername, applicationProperties, svnLoadExternals);
	}
}
