package org.bbop.termgenie.ontology.go.svn;

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

public abstract class AbstractGoCommitSvnModule extends AbstractGoCommitModule {

	private final String svnRepository;
	private final String svnOntologyFileName;

	/**
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param applicationProperties
	 */
	public AbstractGoCommitSvnModule(String svnRepository, String svnOntologyFileName, Properties applicationProperties) {
		super(applicationProperties);
		this.svnRepository = svnRepository;
		this.svnOntologyFileName = svnOntologyFileName;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterSVNRepositoryUrl", svnRepository);
		bind("GeneOntologyCommitAdapterSVNOntologyFileName", svnOntologyFileName);
		bindOBOSCMHelper();
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
	
	protected abstract void bindOBOSCMHelper();

}
