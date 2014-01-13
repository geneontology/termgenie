package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;

import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.ontology.owl.OwlScmHelper;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Main steps for committing ontology changes to an OBO or OWL file in an SVN
 * repository.
 */
public class SvnHelper {

	@Singleton
	public static final class OboSvnHelperPassword extends OboScmHelper {

		private final String svnRepository;
		private final String svnUsername;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		OboSvnHelperPassword(IRIMapper iriMapper,
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
		public VersionControlAdapter createSCM(File svnFolder)
		{
			SvnTool svn = SvnTool.createUsernamePasswordSVN(svnFolder, svnRepository, svnUsername, svnPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}
	}
	
	@Singleton
	public static final class OwlSvnHelperPassword extends OwlScmHelper {

		private final String svnRepository;
		private final String svnUsername;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		OwlSvnHelperPassword(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}

		@Override
		public VersionControlAdapter createSCM(File svnFolder)
		{
			SvnTool svn = SvnTool.createUsernamePasswordSVN(svnFolder, svnRepository, svnUsername, svnPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}
	}

	@Singleton
	public static final class OboSvnHelperAnonymous extends OboScmHelper {

		private final String svnRepository;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		OboSvnHelperAnonymous(IRIMapper iriMapper,
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
		public VersionControlAdapter createSCM(File svnFolder) throws CommitException
		{
			return SvnTool.createAnonymousSVN(svnFolder, svnRepository, svnConfigDir, svnLoadExternals);
		}

	}
	
	@Singleton
	public static final class OwlSvnHelperAnonymous extends OwlScmHelper {

		private final String svnRepository;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;

		@Inject
		OwlSvnHelperAnonymous(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}

		@Override
		public VersionControlAdapter createSCM(File svnFolder) throws CommitException
		{
			return SvnTool.createAnonymousSVN(svnFolder, svnRepository, svnConfigDir, svnLoadExternals);
		}

	}

	@Singleton
	public static final class OboSvnHelperKeyFile extends OboScmHelper {
	
		private final String svnRepository;
		private final String svnUsername;
		private final File svnKeyFile;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;
	
		@Inject
		OboSvnHelperKeyFile(IRIMapper iriMapper,
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
		public VersionControlAdapter createSCM(File svnFolder)
		{
			SvnTool svn = SvnTool.createSSHKeySVN(svnFolder, svnRepository, svnUsername, svnKeyFile, svnPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}
	
	}
	
	@Singleton
	public static final class OwlSvnHelperKeyFile extends OwlScmHelper {
	
		private final String svnRepository;
		private final String svnUsername;
		private final File svnKeyFile;
		private final String svnPassword;
		private final File svnConfigDir;
		private final boolean svnLoadExternals;
	
		@Inject
		OwlSvnHelperKeyFile(IRIMapper iriMapper,
				@Named("CommitAdapterSVNRepositoryUrl") String svnRepository,
				@Named("CommitAdapterSVNOntologyFileName") String svnOntologyFileName,
				@Named("CommitAdapterSVNUsername") String svnUsername,
				@Named("CommitAdapterSVNKeyFile") File svnKeyFile,
				@Named("CommitAdapterSVNPassword") String svnPassword,
				@Named("CommitAdapterSVNConfigDir") File svnConfigDir,
				@Named("CommitAdapterSVNLoadExternals") Boolean svnLoadExternals)
		{
			super(iriMapper, svnOntologyFileName);
			this.svnRepository = svnRepository;
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
			this.svnPassword = svnPassword;
			this.svnConfigDir = svnConfigDir;
			this.svnLoadExternals = svnLoadExternals != null ? svnLoadExternals.booleanValue() : true;
		}
	
		@Override
		public VersionControlAdapter createSCM(File svnFolder)
		{
			SvnTool svn = SvnTool.createSSHKeySVN(svnFolder, svnRepository, svnUsername, svnKeyFile, svnPassword, svnConfigDir, svnLoadExternals);
			return svn;
		}
	
	}

	private SvnHelper() {
		// no instances
	}
}
