package org.bbop.termgenie.ontology.go;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStoreImpl;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GeneOntologyCommitModule extends IOCModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;
	
	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 */
	public GeneOntologyCommitModule(String cvsOntologyFileName, String cvsRoot)
	{
		super();
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		bind(Committer.class).to(GeneOntologyCommitAdapter.class);
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bind("GeneOntologyCommitAdapterCVSPassword"); 
		bind(CommitHistoryStore.class).to(CommitHistoryStoreImpl.class);
	}
	
	@Singleton
	@Provides
	@Named("GeneOntology")
	OntologyTaskManager provideGeneOntology(OntologyConfiguration configuration, OntologyLoader loader) {
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get("GeneOntology");
		return loader.getOntology(configuredOntology);
	}

}
