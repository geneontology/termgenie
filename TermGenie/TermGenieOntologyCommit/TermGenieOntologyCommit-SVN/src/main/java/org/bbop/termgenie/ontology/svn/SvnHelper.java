package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Main steps for committing ontology changes to an OBO file in an SVN
 * repository.
 */
public class SvnHelper {

	@Singleton
	public static final class SvnHelperPassword extends OboScmHelper {

		private final String svnRepository;
		private final String svnUsername;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		SvnHelperPassword(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNAdditionalOntologyFileNames") @Nullable List<String> svnAdditionalOntologyFileNames,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName, svnAdditionalOntologyFileNames);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File svnFolder)
		{
			String realUsername;
			String realPassword;
			if (commitMode == CommitMode.internal) {
				realUsername = svnUsername;
				realPassword = svnPassword;
			}
			else {
				realUsername = username;
				realPassword = password;
			}
			SvnTool svn = SvnTool.createUsernamePasswordSVN(svnFolder, svnRepository, realUsername, realPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}

		@Override
		public boolean isSupportAnonymus() {
			return false;
		}

		@Override
		public CommitMode getCommitMode() {
			return CommitMode.explicit;
		}

		@Override
		public String getCommitUserName() {
			return svnUsername;
		}

		@Override
		public String getCommitPassword() {
			return svnPassword;
		}
	}

	@Singleton
	public static final class SvnHelperAnonymous extends OboScmHelper {

		private final String svnRepository;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		SvnHelperAnonymous(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNAdditionalOntologyFileNames") @Nullable List<String> svnAdditionalOntologyFileNames,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName, svnAdditionalOntologyFileNames);
			this.svnRepository = svnRepository;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File svnFolder) throws CommitException
		{
			return SvnTool.createAnonymousSVN(svnFolder, svnRepository, svnConfigDir, svnLoadExternals);
		}

		@Override
		public boolean isSupportAnonymus() {
			return true;
		}

		@Override
		public CommitMode getCommitMode() {
			return CommitMode.anonymus;
		}

		@Override
		public String getCommitUserName() {
			return null; // no username
		}

		@Override
		public String getCommitPassword() {
			return null; // no password
		}
	}

	@Singleton
	public static final class SvnHelperKeyFile extends OboScmHelper {
	
		private final String svnRepository;
		private final String svnUsername;
		private final File svnKeyFile;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;
	
		@Inject
		SvnHelperKeyFile(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNAdditionalOntologyFileNames") @Nullable List<String> svnAdditionalOntologyFileNames,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNKeyFile") File svnKeyFile,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName, svnAdditionalOntologyFileNames);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}
	
		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File svnFolder)
		{
			String realUsername;
			String realPassword;
			if (commitMode == CommitMode.internal) {
				realUsername = svnUsername;
				realPassword = svnPassword;
			}
			else {
				realUsername = username;
				realPassword = password;
			}
			SvnTool svn = SvnTool.createSSHKeySVN(svnFolder, svnRepository, realUsername, svnKeyFile, realPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}
	
		@Override
		public boolean isSupportAnonymus() {
			return false;
		}
	
		@Override
		public CommitMode getCommitMode() {
			return CommitMode.explicit;
		}
	
		@Override
		public String getCommitUserName() {
			return svnUsername;
		}
	
		@Override
		public String getCommitPassword() {
			return svnPassword;
		}
	}

	private SvnHelper() {
		// no instances
	}
}
