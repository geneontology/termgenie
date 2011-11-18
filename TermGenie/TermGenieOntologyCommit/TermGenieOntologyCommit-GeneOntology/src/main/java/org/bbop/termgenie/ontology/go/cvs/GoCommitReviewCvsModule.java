package org.bbop.termgenie.ontology.go.cvs;

import java.util.Properties;

import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GoCommitReviewCvsModule extends AbstractGoCommitCvsModule {

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties 
	 */
	public GoCommitReviewCvsModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties) {
		super(cvsOntologyFileName, cvsRoot, applicationProperties);
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
