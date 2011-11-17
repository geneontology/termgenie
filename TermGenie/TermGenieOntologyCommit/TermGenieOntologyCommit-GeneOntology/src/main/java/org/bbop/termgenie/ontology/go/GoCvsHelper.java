package org.bbop.termgenie.ontology.go;

import java.io.File;

import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OBOSCMHelper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.scm.VersionControlAdapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Main steps for committing ontology changes to an OBO file in an CVS
 * repository.
 */
public class GoCvsHelper {

	@Singleton
	public static final class GoCvsHelperPassword extends OBOSCMHelper {

		private final String cvsPassword;
		private final String cvsRoot;

		@Inject
		GoCvsHelperPassword(@Named("GeneOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
				@Named("GeneOntologyCommitAdapterCVSPassword") String cvsPassword,
				@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot)
		{
			super(source, iriMapper, cleaner, cvsOntologyFileName);
			this.cvsPassword = cvsPassword;
			this.cvsRoot = cvsRoot;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File cvsFolder)
		{
			String realPassword;
			if (commitMode == CommitMode.internal) {
				realPassword = cvsPassword;
			}
			else {
				realPassword = password;
			}
			CVSTools cvs = new CVSTools(cvsRoot, realPassword, cvsFolder);
			return cvs;
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
			return null; // encoded in the cvs root
		}

		@Override
		public String getCommitPassword() {
			return cvsPassword;
		}
	}

	@Singleton
	public static final class GoCvsHelperAnonymous extends OBOSCMHelper {

		private final String cvsRoot;

		@Inject
		GoCvsHelperAnonymous(@Named("GeneOntology") OntologyTaskManager source,
				IRIMapper iriMapper,
				OntologyCleaner cleaner,
				@Named("GeneOntologyCommitAdapterCVSOntologyFileName") String cvsOntologyFileName,
				@Named("GeneOntologyCommitAdapterCVSRoot") String cvsRoot)
		{
			super(source, iriMapper, cleaner, cvsOntologyFileName);
			this.cvsRoot = cvsRoot;
		}

		@Override
		public VersionControlAdapter createSCM(CommitMode commitMode,
				String username,
				String password,
				File cvsFolder) throws CommitException
		{
			return new CVSTools(cvsRoot, null, cvsFolder);
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
			return null; // encoded in the cvs root
		}

		@Override
		public String getCommitPassword() {
			return null; // no password
		}
	}

	private GoCvsHelper() {
		// no instances
	}
}
