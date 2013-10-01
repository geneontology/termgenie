package org.bbop.termgenie.services.history;

import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.ioc.IOCModule;


public class RecentSubmissionsServiceModule extends IOCModule {

	private final boolean enabled;

	public RecentSubmissionsServiceModule(boolean enabled, Properties applicationProperties) {
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

	protected void bindEnabled() {
		bind(RecentSubmissionsService.class, RecentSubmissionsServiceImpl.class);
	}
	
	protected void bindDisabled() {
		bind(RecentSubmissionsService.class, DisabledRecentSubmissionsServiceImpl.class);
	}

	public static class DisabledRecentSubmissionsServiceImpl implements RecentSubmissionsService {

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public JsonRecentSubmission[] getRecentTerms(String sessionId, HttpSession session) {
			return null;
		}
		
	}
}
