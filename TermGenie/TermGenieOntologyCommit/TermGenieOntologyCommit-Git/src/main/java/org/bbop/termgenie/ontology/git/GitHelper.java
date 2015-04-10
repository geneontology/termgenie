package org.bbop.termgenie.ontology.git;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

import org.bbop.termgenie.git.GitTool;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.ontology.owl.OwlScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public class GitHelper {

	@Singleton
	public static final class OboGitHelperPassword extends OboScmHelper {

		private final String GitRepository;
		private final String GitUsername;
		private final String GitPassword;

		@Inject
		OboGitHelperPassword(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitUsername") String GitUsername,
				@Named("CommitAdapterGitPassword") String GitPassword)
		{
			super(GitOntologyFileName, defaultMappers);
			this.GitRepository = GitRepository;
			this.GitUsername = GitUsername;
			this.GitPassword = GitPassword;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createUsernamePasswordGit(GitFolder, GitRepository, GitUsername, GitPassword);
			return Git;
		}
	}
	
	@Singleton
	public static final class OboGitHelperToken extends OboScmHelper {

		private final String GitRepository;
		private final String GitToken;

		@Inject
		OboGitHelperToken(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitToken") String GitToken)
		{
			super(GitOntologyFileName, defaultMappers);
			this.GitRepository = GitRepository;
			this.GitToken = GitToken;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createUsernamePasswordGit(GitFolder, GitRepository, GitToken, "");
			return Git;
		}
	}
	
	@Singleton
	public static final class OwlGitHelperPassword extends OwlScmHelper {

		private final String GitRepository;
		private final String GitUsername;
		private final String GitPassword;

		@Inject
		OwlGitHelperPassword(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Nullable @Named("CommitAdapterGitCatalogXml") String catalogXml,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitUsername") String GitUsername,
				@Named("CommitAdapterGitPassword") String GitPassword)
		{
			super(GitOntologyFileName, defaultMappers, catalogXml);
			this.GitRepository = GitRepository;
			this.GitUsername = GitUsername;
			this.GitPassword = GitPassword;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createUsernamePasswordGit(GitFolder, GitRepository, GitUsername, GitPassword);
			return Git;
		}
	}
	
	@Singleton
	public static final class OwlGitHelperToken extends OwlScmHelper {

		private final String GitRepository;
		private final String GitToken;

		@Inject
		OwlGitHelperToken(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Nullable @Named("CommitAdapterGitCatalogXml") String catalogXml,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitToken") String GitToken)
		{
			super(GitOntologyFileName, defaultMappers, catalogXml);
			this.GitRepository = GitRepository;
			this.GitToken = GitToken;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createUsernamePasswordGit(GitFolder, GitRepository, GitToken, "");
			return Git;
		}
	}

	@Singleton
	public static final class OboGitHelperAnonymous extends OboScmHelper {

		private final String GitRepository;

		@Inject
		OboGitHelperAnonymous(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName)
		{
			super(GitOntologyFileName, defaultMappers);
			this.GitRepository = GitRepository;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder) throws CommitException
		{
			return GitTool.createAnonymousGit(GitFolder, GitRepository);
		}

	}
	
	@Singleton
	public static final class OwlGitHelperAnonymous extends OwlScmHelper {

		private final String GitRepository;

		@Inject
		OwlGitHelperAnonymous(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Nullable @Named("CommitAdapterGitCatalogXml") String catalogXml,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName)
		{
			super(GitOntologyFileName, defaultMappers, catalogXml);
			this.GitRepository = GitRepository;
		}

		@Override
		public VersionControlAdapter createSCM(File GitFolder) throws CommitException
		{
			return GitTool.createAnonymousGit(GitFolder, GitRepository);
		}

	}

	@Singleton
	public static final class OboGitHelperKeyFile extends OboScmHelper {
	
		private final String GitRepository;
		private final File GitKeyFile;
		private final String GitPassword;
	
		@Inject
		OboGitHelperKeyFile(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitKeyFile") File GitKeyFile,
				@Nullable @Named("CommitAdapterGitPassword") String GitPassword)
		{
			super(GitOntologyFileName, defaultMappers);
			this.GitRepository = GitRepository;
			this.GitKeyFile = GitKeyFile;
			this.GitPassword = GitPassword;
		}
	
		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createSSHKeyGit(GitFolder, GitRepository, GitKeyFile, GitPassword);
			return Git;
		}
	
	}
	
	@Singleton
	public static final class OwlGitHelperKeyFile extends OwlScmHelper {
	
		private final String GitRepository;
		private final File GitKeyFile;
		private final String GitPassword;
	
		@Inject
		OwlGitHelperKeyFile(
				@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers,
				@Nullable @Named("CommitAdapterGitCatalogXml") String catalogXml,
				@Named("CommitAdapterGitRepositoryUrl") String GitRepository,
				@Named("CommitAdapterGitOntologyFileName") String GitOntologyFileName,
				@Named("CommitAdapterGitKeyFile") File GitKeyFile,
				@Nullable @Named("CommitAdapterGitPassword") String GitPassword)
		{
			super(GitOntologyFileName, defaultMappers, catalogXml);
			this.GitRepository = GitRepository;
			this.GitKeyFile = GitKeyFile;
			this.GitPassword = GitPassword;
		}
	
		@Override
		public VersionControlAdapter createSCM(File GitFolder)
		{
			GitTool Git = GitTool.createSSHKeyGit(GitFolder, GitRepository, GitKeyFile, GitPassword);
			return Git;
		}
	
	}

	private GitHelper() {
		// no instances
	}
}
