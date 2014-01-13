package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.obo.DefaultOboTermFilter;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.svn.SvnTool;
import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Provides;
import com.google.inject.Singleton;

abstract class AbstractOboCommitSvnModule extends AbstractCommitModule {

	private final String svnRepository;
	private final String svnOntologyFileName;
	private final List<String> additionalOntologyFileNames;
	private final boolean svnLoadExternals;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param additionalOntologyFileNames
	 * @param svnLoadExternals
	 */
	protected AbstractOboCommitSvnModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			List<String> additionalOntologyFileNames,
			boolean svnLoadExternals)
	{
		super(applicationProperties);
		this.svnRepository = svnRepository;
		this.svnOntologyFileName = svnOntologyFileName;
		this.additionalOntologyFileNames = additionalOntologyFileNames;
		this.svnLoadExternals = svnLoadExternals;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNRepositoryUrl", svnRepository);
		bind("CommitAdapterSVNOntologyFileName", svnOntologyFileName);
		bindList("CommitAdapterSVNAdditionalOntologyFileNames", additionalOntologyFileNames, true);
		bind("CommitAdapterSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bind("CommitAdapterSVNLoadExternals", svnLoadExternals);
		bindScmHelper();
	}

	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(OntologyLoader loader,
			CommitHistoryStore store,
			TermFilter<OBODoc> filter,
			ReviewMailHandler handler,
			OboScmHelper helper)
	{
		return new OboCommitReviewPipeline(loader.getOntologyManager(), store, filter, handler, helper);
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}

	protected abstract void bindScmHelper();

	@Singleton
	@Provides
	protected TermFilter<OBODoc> provideTermFilter() {
		return new DefaultOboTermFilter();
	}
}
