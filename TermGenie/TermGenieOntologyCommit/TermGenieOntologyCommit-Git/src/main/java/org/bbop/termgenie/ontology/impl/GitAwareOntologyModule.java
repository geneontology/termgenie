package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;

import org.bbop.termgenie.git.GitTool;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public class GitAwareOntologyModule extends OntologyModule {

	public static GitAwareOntologyModule createAnonymousGitModule(String configFile, Properties applicationProperties)
	{
		return new AnonymousGitOntologyModule(configFile, applicationProperties);
	}
	
	public static GitAwareOntologyModule createUsernamePasswordGitModule(String configFile,
			Properties applicationProperties,
			String gitUsername)
	{
		PasswordGitOntologyModule m = new PasswordGitOntologyModule(configFile, applicationProperties);
		m.setGitUsername(gitUsername);
		return m;
	}
	
	public static GitAwareOntologyModule createSshKeyGitModule(String configFile,
			Properties applicationProperties,
			String keyFile,
			boolean usePassphrase)
	{
		KeyGitOntologyModule m = new KeyGitOntologyModule(configFile, applicationProperties);
		m.setGitKeyFile(keyFile);
		m.setUsePassphrase(usePassphrase);
		return m;
	}

	private String gitAwareRepositoryURL = null;
	private Map<IRI, String> gitAwareMappedIRIs = Collections.emptyMap();
	private String gitAwareCatalogXML = null;
	private String gitAwareWorkFolder = null;
	
	private GitAwareOntologyModule(String configFile, Properties applicationProperties) {
		super(applicationProperties, configFile);
	}
	
	public void setGitAwareRepositoryURL(String repositoryURL) {
		this.gitAwareRepositoryURL = repositoryURL;
	}

	public void setGitAwareMappedIRIs(Map<IRI, String> mappedIRIs) {
		this.gitAwareMappedIRIs = mappedIRIs;
	}
	
	public void setGitAwareCatalogXML(String catalogXML) {
		this.gitAwareCatalogXML = catalogXML;
	}
	
	public void setGitAwareWorkFolder(String workFolder) {
		this.gitAwareWorkFolder = workFolder;
	}
	
	static final String GitAwareIRIMapperRepositoryURL = "GitAwareIRIMapperRepositoryURL";
	static final String GitAwareIRIMapperWorkFolder = "GitAwareIRIMapperWorkFolder";
	static final String GitAwareIRIMapperMappedIRIs = "GitAwareIRIMapperMappedIRIs";
	static final String GitAwareIRIMapperCatalogXML = "GitAwareIRIMapperCatalogXML";
	
	@Override
	protected void bindIRIMappers() {
		super.bindIRIMappers();
		bind(GitAwareIRIMapperRepositoryURL, gitAwareRepositoryURL);
		bind(GitAwareIRIMapperWorkFolder, gitAwareWorkFolder);
		bindIRIMap(GitAwareIRIMapperMappedIRIs, gitAwareMappedIRIs);
		bind(GitAwareIRIMapperCatalogXML, gitAwareCatalogXML, true);
		bindAdditional();
	}
	
	protected void bindAdditional() {
		// default empty;
	}

	static class AnonymousGitOntologyModule extends GitAwareOntologyModule {

		AnonymousGitOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}

		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named(GitAwareIRIMapperRepositoryURL) String repositoryURL,
				@Named(GitAwareIRIMapperCatalogXML) String catalogXML,
				@Named(GitAwareIRIMapperMappedIRIs) Map<IRI, String> mappedIRIs,
				@Named(GitAwareIRIMapperWorkFolder) @Nullable String gitWorkFolder)
		{
			final File workFolderFile = new File(gitWorkFolder);
			final GitTool git = GitTool.createAnonymousGit(workFolderFile, repositoryURL);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			GitIRIMapper gitMapper = new GitIRIMapper(git, checkout, mappedIRIs, catalogXML);
			return gitMapper;
		}
	}
	
	static class PasswordGitOntologyModule extends GitAwareOntologyModule {

		private String gitUsername = null;

		PasswordGitOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}
		
		public void setGitUsername(String gitUsername) {
			this.gitUsername = gitUsername;
		}

		@Override
		protected void bindAdditional() {
			bind(GitAwareIRIMapperUsername, gitUsername);
			bindSecret(GitAwareIRIMapperPassword);
		}

		static final String GitAwareIRIMapperUsername = "GitAwareIRIMapperUsername";
		static final String GitAwareIRIMapperPassword = "GitAwareIRIMapperPassword";

		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named(GitAwareIRIMapperRepositoryURL) String repositoryURL,
				@Named(GitAwareIRIMapperCatalogXML) String catalogXML,
				@Named(GitAwareIRIMapperMappedIRIs) Map<IRI, String> mappedIRIs,
				@Named(GitAwareIRIMapperWorkFolder) @Nullable String workFolder,
				@Named(GitAwareIRIMapperUsername) String gitUsername,
				@Named(GitAwareIRIMapperPassword) String gitPassword)
		{
			final File workFolderFile = new File(workFolder);
			final GitTool git = GitTool.createUsernamePasswordGit(workFolderFile, repositoryURL, gitUsername, gitPassword);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			GitIRIMapper gitMapper = new GitIRIMapper(git, checkout, mappedIRIs, catalogXML);
			return gitMapper;
		}
	}
	
	static class KeyGitOntologyModule extends GitAwareOntologyModule {

		private String gitKeyFile = null;
		private boolean usePassphrase = true;

		KeyGitOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}
		
		public void setGitKeyFile(String gitKeyFile) {
			this.gitKeyFile = gitKeyFile;
		}
		
		public void setUsePassphrase(boolean usePassphrase) {
			this.usePassphrase = usePassphrase;
		}

		static final String GitAwareIRIMapperKeyFile = "GitAwareIRIMapperKeyFile";
		static final String GitAwareIRIMapperPassword = "GitAwareIRIMapperPassword";
		
		@Override
		protected void bindAdditional() {
			bind(GitAwareIRIMapperKeyFile, gitKeyFile);
			if (usePassphrase) {
				bindSecret(GitAwareIRIMapperPassword);	
			}
			else {
				bindNull(GitAwareIRIMapperPassword);
			}
		}

		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named(GitAwareIRIMapperRepositoryURL) String repositoryURL,
				@Named(GitAwareIRIMapperCatalogXML) String catalogXML,
				@Named(GitAwareIRIMapperMappedIRIs) Map<IRI, String> mappedIRIs,
				@Named(GitAwareIRIMapperWorkFolder) @Nullable String workFolder,
				@Named(GitAwareIRIMapperKeyFile) String gitKeyFile,
				@Named(GitAwareIRIMapperPassword) @Nullable String gitPassword)
		{
			final File workFolderFile = new File(workFolder);
			final File keyFile = new File(gitKeyFile);
			final GitTool git = GitTool.createSSHKeyGit(workFolderFile, repositoryURL, keyFile, gitPassword);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			GitIRIMapper gitMapper = new GitIRIMapper(git, checkout, mappedIRIs, catalogXML);
			return gitMapper;
		}
	}

}
