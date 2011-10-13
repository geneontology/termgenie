package org.bbop.termgenie.ontology.go;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStoreImpl;
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
	 */
	public GeneOntologyCommitReviewModule(String cvsOntologyFileName, String cvsRoot) {
		super();
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bind("GeneOntologyCommitAdapterCVSPassword");
		bind(CommitHistoryStore.class).to(CommitHistoryStoreImpl.class);
		bind(GoCvsHelper.class).to(GoCvsHelper.GoCvsHelperPassword.class);
	}

	@Singleton
	@Provides
	@Named("GeneOntologyTaskManager")
	OntologyTaskManager provideGeneOntologyManager(@Named("ConfiguredOntologyGeneOntology") ConfiguredOntology configuration,
			OntologyLoader loader)
	{
		OntologyTaskManager manager = loader.getOntology(configuration);
		return manager;
	}

	@Singleton
	@Provides
	@Named("ConfiguredOntologyGeneOntology")
	ConfiguredOntology provideGeneOntologyConfiguration(OntologyConfiguration configuration) {
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get("GeneOntology");
		return configuredOntology;
	}

}
