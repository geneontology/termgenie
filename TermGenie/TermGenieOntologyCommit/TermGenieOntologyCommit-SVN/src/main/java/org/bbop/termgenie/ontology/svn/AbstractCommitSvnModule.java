package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Provides;
import com.google.inject.Singleton;

abstract class AbstractCommitSvnModule extends AbstractCommitModule {

	private final String svnRepository;
	private final String svnOntologyFileName;
	private final boolean svnLoadExternals;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param svnLoadExternals
	 */
	protected AbstractCommitSvnModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			boolean svnLoadExternals)
	{
		super(applicationProperties);
		this.svnRepository = svnRepository;
		this.svnOntologyFileName = svnOntologyFileName;
		this.svnLoadExternals = svnLoadExternals;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNRepositoryUrl", svnRepository);
		bind("CommitAdapterSVNOntologyFileName", svnOntologyFileName);
		bind("CommitAdapterSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bind("CommitAdapterSVNLoadExternals", svnLoadExternals);
		bindScmHelper();
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}

	protected abstract void bindScmHelper();

}
