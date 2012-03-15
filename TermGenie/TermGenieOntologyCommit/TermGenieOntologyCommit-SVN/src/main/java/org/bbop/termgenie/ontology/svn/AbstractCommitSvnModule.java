package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

abstract class AbstractCommitSvnModule extends AbstractCommitModule {

	private final String svnRepository;
	private final String svnOntologyFileName;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param commitTargetOntologyName
	 */
	protected AbstractCommitSvnModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			String commitTargetOntologyName)
	{
		super(applicationProperties, commitTargetOntologyName);
		this.svnRepository = svnRepository;
		this.svnOntologyFileName = svnOntologyFileName;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNRepositoryUrl", svnRepository);
		bind("CommitAdapterSVNOntologyFileName", svnOntologyFileName);
		bind("CommitAdapterSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bindOBOSCMHelper();
	}

	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(@Named("CommitTargetOntology") OntologyTaskManager source,
			CommitHistoryStore store,
			OboScmHelper helper)
	{
		return new OboCommitReviewPipeline(source, store, helper);
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}

	protected abstract void bindOBOSCMHelper();

}
