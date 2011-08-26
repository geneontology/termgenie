package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Singleton;

public class NoopCommitModule extends IOCModule {

	@Override
	protected void configure() {
		bind(Committer.class).to(EmptyCommitter.class);
	}
	
	@Singleton
	private static class EmptyCommitter implements Committer {

		@Override
		public boolean commit(CommitInfo commitInfo) throws CommitException {
			return false;
		}
	}

}
