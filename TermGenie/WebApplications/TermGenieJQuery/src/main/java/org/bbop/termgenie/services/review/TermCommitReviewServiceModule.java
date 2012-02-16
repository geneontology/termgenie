package org.bbop.termgenie.services.review;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


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
	
}
