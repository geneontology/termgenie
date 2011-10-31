package org.bbop.termgenie.ontology.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStoreImpl;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GeneOntologyCommitReviewModule extends IOCModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties 
	 */
	public GeneOntologyCommitReviewModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties) {
		super(applicationProperties);
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		bindCVSPassword();
		bind(CommitHistoryStore.class).to(CommitHistoryStoreImpl.class);
		bindGoCvsHelper();
		bindReviewStages();
	}

	protected void bindReviewStages() {
		bind(OntologyCommitReviewPipelineStages.class).to(GeneOntologyReviewCommitAdapter.class);
	}

	protected void bindCVSPassword() {
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bind("GeneOntologyCommitAdapterCVSPassword");
	}

	protected void bindGoCvsHelper() {
		bind(GoCvsHelper.class).to(GoCvsHelper.GoCvsHelperPassword.class);
	}

	@Singleton
	@Provides
	@Named("GeneOntology")
	OntologyTaskManager provideGeneOntologyManager(OntologyConfiguration configuration,
			OntologyLoader loader)
	{
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager manager = loader.getOntology(configuredOntology);
		return manager;
	}

	@Singleton
	@Provides
	Committer provideReviewCommitter(OntologyCommitReviewPipelineStages pipeline) {
		return pipeline.getReviewCommitter();
	}
}
