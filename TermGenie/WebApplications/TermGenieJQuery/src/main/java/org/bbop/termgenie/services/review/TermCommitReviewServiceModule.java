package org.bbop.termgenie.services.review;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public final class TermCommitReviewServiceModule extends IOCModule {

	private final boolean enabled;
	private final Class<? extends TermCommitReviewService> enabledService;
	
	private boolean doAsciiCheck = false;
	private boolean useOboDiff = true;
	
	/**
	 * @param enabled
	 * @param applicationProperties
	 */
	public TermCommitReviewServiceModule(boolean enabled, Properties applicationProperties) {
		this(enabled, TermCommitReviewServiceImpl.class, applicationProperties);
	}
	
	/**
	 * @param enabled
	 * @param enabledService
	 * @param applicationProperties
	 */
	public TermCommitReviewServiceModule(boolean enabled, 
			Class<? extends TermCommitReviewService> enabledService,
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.enabled = enabled;
		this.enabledService = enabledService;
	}
	
	/**
	 * @param doAsciiCheck the doAsciiCheck to set
	 */
	public void setDoAsciiCheck(boolean doAsciiCheck) {
		this.doAsciiCheck = doAsciiCheck;
	}
	
	/**
	 * @param useOboDiff the useOboDiff to set
	 */
	public void setUseOboDiff(boolean useOboDiff) {
		this.useOboDiff = useOboDiff;
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
		bind(TermCommitReviewService.class, enabledService);
		bind("TermCommitReviewServiceImpl.doAsciiCheck", doAsciiCheck);
		bind("TermCommitReviewServiceImpl.useOboDiff", useOboDiff);
	}
	
}
