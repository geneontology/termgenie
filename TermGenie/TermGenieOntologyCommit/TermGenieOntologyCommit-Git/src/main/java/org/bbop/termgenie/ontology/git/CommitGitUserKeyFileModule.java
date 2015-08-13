package org.bbop.termgenie.ontology.git;

import java.io.File;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitGitUserKeyFileModule {

	private static class CommitOboGitUserKeyFileModule extends AbstractOboCommitGitModule {
		private final String gitUsername;
		private final File gitKeyFile;
		private final boolean usePassphrase;
	
		private CommitOboGitUserKeyFileModule(String gitRepository,
				String gitOntologyFileName,
				String gitUsername,
				File gitKeyFile,
				Properties applicationProperties,
				boolean usePassphrase)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.gitUsername = gitUsername;
			this.gitKeyFile = gitKeyFile;
			this.usePassphrase = usePassphrase;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterGitUsername", gitUsername);
			bind("CommitAdapterGitKeyFile", gitKeyFile);
			if (usePassphrase) {
				bindSecret("CommitAdapterGitPassword");
			}
			else {
				bindNull("CommitAdapterGitPassword");
			}
		}
	
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					GitHelper.OboGitHelperKeyFile.class);
		}
	}
	
	private static class CommitOwlGitUserKeyFileModule extends AbstractOwlCommitGitModule {
		private final String gitUsername;
		private final File gitKeyFile;
		private final boolean usePassphrase;
		private final String catalogXml;
	
		private CommitOwlGitUserKeyFileModule(String gitRepository,
				String gitOntologyFileName,
				String catalogXml,
				String gitUsername,
				File gitKeyFile,
				Properties applicationProperties,
				boolean usePassphrase)
		{
			super(gitRepository, gitOntologyFileName, applicationProperties);
			this.catalogXml = catalogXml;
			this.gitUsername = gitUsername;
			this.gitKeyFile = gitKeyFile;
			this.usePassphrase = usePassphrase;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterGitUsername", gitUsername);
			bind("CommitAdapterGitKeyFile", gitKeyFile);
			if (usePassphrase) {
				bindSecret("CommitAdapterGitPassword");	
			}
			else {
				bindNull("CommitAdapterGitPassword");
			}
			bind("CommitAdapterGitCatalogXml", catalogXml, true, true);
		}
	
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					GitHelper.OwlGitHelperKeyFile.class);
		}
	}
	
	/**
	 * Create an commit module for OBO and Git with public/private key authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param gitUsername
	 * @param gitKeyFile
	 * @param applicationProperties
	 * @param usePassphrase
	 * @return module
	 */
	public static IOCModule createOboModule(String gitRepository,
			String gitOntologyFileName,
			String gitUsername,
			File gitKeyFile,
			Properties applicationProperties,
			boolean usePassphrase) {
		return new CommitOboGitUserKeyFileModule(gitRepository, gitOntologyFileName, gitUsername, gitKeyFile, applicationProperties, usePassphrase);
	}
	
	/**
	 * Create an commit module for OWL and Git with public/private key authentication.
	 * 
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param catalogXml
	 * @param gitUsername
	 * @param gitKeyFile
	 * @param applicationProperties
	 * @param usePassphrase
	 * @return module
	 */
	public static IOCModule createOwlModule(String gitRepository,
			String gitOntologyFileName,
			String catalogXml,
			String gitUsername,
			File gitKeyFile,
			Properties applicationProperties,
			boolean usePassphrase) {
		return new CommitOwlGitUserKeyFileModule(gitRepository, gitOntologyFileName, catalogXml, gitUsername, gitKeyFile, applicationProperties, usePassphrase);
	}
}
