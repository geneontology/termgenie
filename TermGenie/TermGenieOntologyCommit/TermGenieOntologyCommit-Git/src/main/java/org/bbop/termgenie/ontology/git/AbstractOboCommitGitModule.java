package org.bbop.termgenie.ontology.git;

import java.util.Properties;

import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Provides;
import com.google.inject.Singleton;


abstract class AbstractOboCommitGitModule extends AbstractCommitGitModule {

	/**
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param applicationProperties
	 */
	protected AbstractOboCommitGitModule(String gitRepository,
			String gitOntologyFileName,
			Properties applicationProperties)
	{
		super(gitRepository, gitOntologyFileName, applicationProperties);
	}

	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(OntologyLoader loader,
			CommitHistoryStore store,
			ReviewMailHandler handler,
			ScmHelper<OBODoc> helper)
	{
		return new OboCommitReviewPipeline(loader.getOntologyManager(), store, handler, helper);
	}
}
