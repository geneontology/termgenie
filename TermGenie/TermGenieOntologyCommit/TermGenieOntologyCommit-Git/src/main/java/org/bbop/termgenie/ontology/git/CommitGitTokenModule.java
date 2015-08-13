package org.bbop.termgenie.ontology.git;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitGitTokenModule {

	private static class CommitOboGitTokenModule extends AbstractOboCommitGitModule {

		private CommitOboGitTokenModule(String gitRepository,
				String gitOntologyFileName,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
		}

		@Override
		protected void configure() {
			super.configure();
			bindSecret("CommitAdapterGitToken");
		}

		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					GitHelper.OboGitHelperToken.class);
		}
	}

	private static class CommitOwlGitTokenModule extends AbstractOwlCommitGitModule {

		private final String catalogXml;

		private CommitOwlGitTokenModule(String gitRepository,
				String gitOntologyFileName,
				String catalogXml,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.catalogXml = catalogXml;
		}

		@Override
		protected void configure() {
			super.configure();
			bindSecret("CommitAdapterGitToken");
			bind("CommitAdapterGitCatalogXml", catalogXml, true, true);
		}

		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					GitHelper.OwlGitHelperToken.class);
		}
	}

	/**
	 * Create an commit module for OBO and Git with username and password authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param applicationProperties
	 * @return module
	 */
	public static IOCModule createOboModule(String gitRepository,
			String gitOntologyFileName,
			Properties applicationProperties)
	{
		return new CommitOboGitTokenModule(gitRepository, gitOntologyFileName, applicationProperties);
	}

	/**
	 * Create an commit module for OWL and Git with username and password authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param catalogXml
	 * @param applicationProperties
	 * @return module
	 */
	public static IOCModule createOwlModule(String gitRepository,
			String gitOntologyFileName,
			String catalogXml,
			Properties applicationProperties)
	{
		return new CommitOwlGitTokenModule(gitRepository, gitOntologyFileName, catalogXml, applicationProperties);
	}
}
