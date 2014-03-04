package org.bbop.termgenie.ontology.svn;

import java.util.Properties;

import org.bbop.termgenie.mail.review.ReviewMailHandler;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.owl.OwlCommitReviewPipeline;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.Provides;
import com.google.inject.Singleton;


abstract class AbstractOwlCommitSvnModule extends AbstractCommitSvnModule {

	protected AbstractOwlCommitSvnModule(String svnRepository,
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
			ScmHelper<OWLOntology> helper)
	{
		return new OwlCommitReviewPipeline(loader.getOntologyManager(), store, handler, helper);
	}
	
}
