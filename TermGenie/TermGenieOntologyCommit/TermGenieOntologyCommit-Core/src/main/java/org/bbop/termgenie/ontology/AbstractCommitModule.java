package org.bbop.termgenie.ontology;

import java.util.Properties;

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

public abstract class AbstractCommitModule extends IOCModule {
	
	private final String commitTargetOntologyName;

	protected AbstractCommitModule(Properties applicationProperties, String commitTargetOntologyName) {
		super(applicationProperties);
		this.commitTargetOntologyName = commitTargetOntologyName;
	}

	@Override
	protected void configure() {
		bind(CommitHistoryStore.class, CommitHistoryStoreImpl.class);
	}

	@Singleton
	@Provides
	@Named("CommitTargetOntology")
	protected OntologyTaskManager provideGeneOntology(OntologyConfiguration configuration,
			OntologyLoader loader)
	{
		ConfiguredOntology configuredOntology = configuration.getOntologyConfigurations().get(commitTargetOntologyName);
		return loader.getOntology(configuredOntology);
	}
	
}
