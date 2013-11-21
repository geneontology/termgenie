package org.bbop.termgenie.ontology;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public abstract class AbstractCommitModule extends IOCModule {
	
	protected AbstractCommitModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(CommitHistoryStore.class, CommitHistoryStoreImpl.class);
	}

}
