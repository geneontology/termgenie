package org.bbop.termgenie.ontology.git;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitGitAnonymousModule {

	private static class CommitOboGitAnonymousModule extends AbstractOboCommitGitModule {
	
		private CommitOboGitAnonymousModule(String gitRepository,
				String gitOntologyFileName,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
		}
		
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					GitHelper.OboGitHelperAnonymous.class);
		}
	}

	private static class CommitOwlGitAnonymousModule extends AbstractOwlCommitGitModule {
		
		private final String catalogXml;

		private CommitOwlGitAnonymousModule(String gitRepository,
				String gitOntologyFileName,
				String catalogXml,
				Properties applicationProperties)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.catalogXml = catalogXml;
		}
		
		@Override
		protected void bindScmHelper() {
			bind("CommitAdapterGitCatalogXml", catalogXml, true);
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					GitHelper.OwlGitHelperAnonymous.class);
		}
	}
	

	/**
	 * Create an commit module for OBO and Git without authentication.
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
		return new CommitOboGitAnonymousModule(gitRepository, gitOntologyFileName, applicationProperties);
	}
	
	/**
	 * Create an commit module for OWL and Git without authentication.
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
		return new CommitOwlGitAnonymousModule(gitRepository, gitOntologyFileName, catalogXml, applicationProperties);
	}
}
