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
	 * @param name 
	 */
	public TermCommitReviewServiceModule(boolean enabled, Properties applicationProperties, String name) {
		super(applicationProperties, name, null);
		this.enabled = enabled;
	}
	
	/**
	 * @param enabled
	 * @param applicationProperties
	 */
	public TermCommitReviewServiceModule(boolean enabled, Properties applicationProperties) {
		this(enabled, applicationProperties, TermCommitReviewServiceModule.class.getSimpleName());
	}

	@Override
	protected void configure() {
		if (enabled) {
			bind(TermCommitReviewService.class, TermCommitReviewServiceImpl.class);
		}
		else {
			bind(TermCommitReviewService.class, DisabledTermCommitReviewServiceImpl.class);
		}
	}
	
	@SuppressWarnings("unused")
	@Named("TermCommitReviewServiceOntology")
	@Provides
	@Singleton
	protected OntologyTaskManager getTermCommitReviewServiceOntology(OntologyConfiguration configuration, OntologyLoader ontologyLoader){
		throw new RuntimeException("This method must be overwritten, if reviews are enabled");
	}

}
