package org.bbop.termgenie.ontology.svn;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.ontology.AbstractCommitModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.obo.DefaultOboTermFilter;
import org.bbop.termgenie.ontology.obo.OboCommitReviewPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;
import org.bbop.termgenie.svn.SvnTool;
import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

abstract class AbstractCommitSvnModule extends AbstractCommitModule {

	private final String svnRepository;
	private final String svnOntologyFileName;
	private final List<String> additionalOntologyFileNames;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 * @param commitTargetOntologyName
	 */
	protected AbstractCommitSvnModule(String svnRepository,
			String svnOntologyFileName,
			Properties applicationProperties,
			String commitTargetOntologyName,
			List<String> additionalOntologyFileNames)
	{
		super(applicationProperties, commitTargetOntologyName);
		this.svnRepository = svnRepository;
		this.svnOntologyFileName = svnOntologyFileName;
		this.additionalOntologyFileNames = additionalOntologyFileNames;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("CommitAdapterSVNRepositoryUrl", svnRepository);
		bind("CommitAdapterSVNOntologyFileName", svnOntologyFileName);
		bindList("CommitAdapterSVNAdditionalOntologyFileNames", additionalOntologyFileNames, true);
		bind("CommitAdapterSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bindOBOSCMHelper();
	}

	@Singleton
	@Provides
	protected OntologyCommitReviewPipelineStages provideReviewStages(@Named("CommitTargetOntology") OntologyTaskManager source,
			CommitHistoryStore store,
			TermFilter<OBODoc> filter,
			OboScmHelper helper)
	{
		return new OboCommitReviewPipeline(source, store, filter, helper);
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}

	protected abstract void bindOBOSCMHelper();

	@Singleton
	@Provides
	protected TermFilter<OBODoc> provideTermFilter() {
		return new DefaultOboTermFilter();
	}
}
