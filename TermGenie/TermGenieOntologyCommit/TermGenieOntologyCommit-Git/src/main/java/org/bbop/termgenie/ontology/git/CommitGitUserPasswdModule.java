package org.bbop.termgenie.ontology.git;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitGitUserPasswdModule {

	private static class CommitOboGitUserPasswdModule extends AbstractOboCommitGitModule {

		private final String gitUsername;

		private CommitOboGitUserPasswdModule(String gitRepository,
				String gitOntologyFileName,
				String gitUsername,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.gitUsername = gitUsername;
		}

		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterGitUsername", gitUsername);
			bindSecret("CommitAdapterGitPassword");
		}

		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					GitHelper.OboGitHelperPassword.class);
		}
	}

	private static class CommitOwlGitUserPasswdModule extends AbstractOwlCommitGitModule {

		private final String gitUsername;
		private final String catalogXml;

		private CommitOwlGitUserPasswdModule(String gitRepository,
				String gitOntologyFileName,
				String catalogXml,
				String gitUsername,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.catalogXml = catalogXml;
			this.gitUsername = gitUsername;
		}

		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterGitUsername", gitUsername);
			bindSecret("CommitAdapterGitPassword");
			bind("CommitAdapterGitCatalogXml", catalogXml, true);
		}

		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					GitHelper.OwlGitHelperPassword.class);
		}
	}

	/**
	 * Create an commit module for OBO and Git with username and password authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param gitUsername
	 * @param applicationProperties
	 * @return module
	 */
	public static IOCModule createOboModule(String gitRepository,
			String gitOntologyFileName,
			String gitUsername,
			Properties applicationProperties)
	{
		return new CommitOboGitUserPasswdModule(gitRepository, gitOntologyFileName, gitUsername, applicationProperties);
	}

	/**
	 * Create an commit module for OWL and Git with username and password authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param catalogXml
	 * @param gitUsername
	 * @param applicationProperties
	 * @return module
	 */
	public static IOCModule createOwlModule(String gitRepository,
			String gitOntologyFileName,
			String catalogXml,
			String gitUsername,
			Properties applicationProperties)
	{
		return new CommitOwlGitUserPasswdModule(gitRepository, gitOntologyFileName, catalogXml, gitUsername, applicationProperties);
	}
}
