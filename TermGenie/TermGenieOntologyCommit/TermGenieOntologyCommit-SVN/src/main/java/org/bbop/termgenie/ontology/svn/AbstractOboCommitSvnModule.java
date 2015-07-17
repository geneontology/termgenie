package org.bbop.termgenie.ontology.svn;

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

abstract class AbstractOboCommitSvnModule extends AbstractCommitSvnModule {

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param svnLoadExternals
	 */
	protected AbstractOboCommitSvnModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
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
