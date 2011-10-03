package org.bbop.termgenie.ontology.go;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStoreImpl;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GeneOntologyFileOnlyCommitModule extends IOCModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;
	private final String localFile;
	
	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param localFile
	 */
	public GeneOntologyFileOnlyCommitModule(String cvsOntologyFileName, String cvsRoot, String localFile)
	{
		super();
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
		this.localFile = localFile;
	}

	@Override
	protected void configure() {
		bind(Committer.class).to(FileOnlyGeneOntologyCommitAdapter.class);
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		bind("FileOnlyGeneOntologyCommitAdapterLocalFile", localFile);
		bind(CommitHistoryStore.class).to(CommitHistoryStoreImpl.class);
	}
	
	@Singleton
	@Provides
	@Named("ConfiguredOntologyGeneOntology")
	ConfiguredOntology provideGeneOntology(OntologyConfiguration configuration) {
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get("GeneOntology");
		return configuredOntology;
	}

}
