package org.bbop.termgenie.ontology.svn;

import java.io.File;

import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyTaskManager;
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

		@Inject
		SvnHelperPassword(@Named("CommitTargetOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir)
		{
			super(source, iriMapper, cleaner, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
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
			SvnTool svn = SvnTool.createUsernamePasswordSVN(svnFolder, svnRepository, realUsername, realPassword, svnConfigDir);
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

		@Inject
		SvnHelperAnonymous(@Named("CommitTargetOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir)
		{
			super(source, iriMapper, cleaner, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnConfigDir = svnConfigDir;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File svnFolder) throws CommitException
		{
			return SvnTool.createAnonymousSVN(svnFolder, svnRepository, svnConfigDir);
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
	
		@Inject
		SvnHelperKeyFile(@Named("CommitTargetOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNKeyFile") File svnKeyFile,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir)
		{
			super(source, iriMapper, cleaner, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
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
			SvnTool svn = SvnTool.createSSHKeySVN(svnFolder, svnRepository, realUsername, svnKeyFile, realPassword, svnConfigDir);
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
