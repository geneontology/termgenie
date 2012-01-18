package org.bbop.termgenie.services.review;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public class TermCommitReviewServiceModule extends IOCModule {

	private final boolean enabled;
	
	/**
	 * @param enabled
	 * @param applicationProperties
	 */
	public TermCommitReviewServiceModule(boolean enabled, Properties applicationProperties) {
		super(applicationProperties);
		this.enabled = enabled;
	}
	
	@Override
	protected void configure() {
		if (enabled) {
			bindEnabled();
		}
		else {
			bindDisabled();
		}
	}

	protected void bindDisabled() {
		bind(TermCommitReviewService.class, DisabledTermCommitReviewServiceImpl.class);
	}

	protected void bindEnabled() {
		bind(TermCommitReviewService.class, TermCommitReviewServiceImpl.class);
	}
	
	/**
	 * This method must be overwritten and implemented, if reviews are enabled.
	 * 
	 * @param configuration
	 * @param ontologyLoader
	 * @return ontology
	 */
	@Named("TermCommitReviewServiceOntology")
	@Provides
	@Singleton
	protected OntologyTaskManager getTermCommitReviewServiceOntology(OntologyConfiguration configuration, OntologyLoader ontologyLoader){
		throw new RuntimeException("This method must be overwritten, if reviews are enabled");
	}

}
