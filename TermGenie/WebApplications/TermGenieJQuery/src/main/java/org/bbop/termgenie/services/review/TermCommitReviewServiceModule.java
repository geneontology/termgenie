package org.bbop.termgenie.services.review;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public class TermCommitReviewServiceModule extends IOCModule {

	private final boolean enabled;
	
	/**
	 * @param enabled
	 */
	public TermCommitReviewServiceModule(boolean enabled) {
		super();
		this.enabled = enabled;
	}

	@Override
	protected void configure() {
		if (enabled) {
			bind(TermCommitReviewService.class).to(TermCommitReviewServiceImpl.class);
		}
		else {
			bind(TermCommitReviewService.class).to(DisabledTermCommitReviewServiceImpl.class);
		}
	}
	
	@SuppressWarnings("unused")
	@Named("TermCommitReviewServiceOntology")
	@Provides
	@Singleton
	protected Ontology getTermCommitReviewServiceOntology(OntologyConfiguration configuration){
		throw new RuntimeException("This method must be overwritten, if reviews are enabled");
	}

}
