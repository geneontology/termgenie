package org.bbop.termgenie.ontology.go.cvs;

import java.util.Properties;

import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.go.AbstractGoCommitModule;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GoCommitReviewCvsModule extends AbstractGoCommitModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties 
	 */
	public GoCommitReviewCvsModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties) {
		super(applicationProperties);
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		bindCVSPassword();
		bindOBOSCMHelper();
	}
	
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, GoCvsHelperPassword.class);
	}

	protected void bindCVSPassword() {
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bindSecret("GeneOntologyCommitAdapterCVSPassword");
	}
	
	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(@Named("GeneOntology") OntologyTaskManager source,
			CommitHistoryStore store,
			OboScmHelper helper) {
		return new OboCommitReviewPipeline(source, store, helper);
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}
}
