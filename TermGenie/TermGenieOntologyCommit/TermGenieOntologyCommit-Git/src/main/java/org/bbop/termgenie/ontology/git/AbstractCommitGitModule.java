package org.bbop.termgenie.ontology.git;

import java.util.Properties;

import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;

import com.google.inject.Provides;
import com.google.inject.Singleton;


abstract class AbstractCommitGitModule extends AbstractCommitModule {

	private final String gitRepository;
	private final String gitOntologyFileName;

	/**
	 * @param gitRepository
	 * @param gitOntologyFileName
	 * @param applicationProperties
	 */
	protected AbstractCommitGitModule(String gitRepository,
			String gitOntologyFileName,
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.gitRepository = gitRepository;
		this.gitOntologyFileName = gitOntologyFileName;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterGitRepositoryUrl", gitRepository);
		bind("CommitAdapterGitOntologyFileName", gitOntologyFileName);
		bindScmHelper();
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}

	protected abstract void bindScmHelper();
}
