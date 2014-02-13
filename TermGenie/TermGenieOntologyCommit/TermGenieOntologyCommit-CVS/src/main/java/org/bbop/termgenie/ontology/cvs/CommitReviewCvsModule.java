package org.bbop.termgenie.ontology.cvs;

import java.util.Properties;

import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CommitReviewCvsModule extends AbstractCommitModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties 
	 */
	public CommitReviewCvsModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties) {
		super(applicationProperties);
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("CommitAdapterCVSRoot", cvsRoot);
		bindCVSPassword();
		bindOBOSCMHelper();
	}
	
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, CvsHelperPassword.class);
	}

	protected void bindCVSPassword() {
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bindSecret("CommitAdapterCVSPassword");
	}
	
	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(OntologyTaskManager source,
			CommitHistoryStore store,
			ReviewMailHandler handler,
			OboScmHelper helper) {
		return new OboCommitReviewPipeline(source, store, handler, helper);
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}
}
