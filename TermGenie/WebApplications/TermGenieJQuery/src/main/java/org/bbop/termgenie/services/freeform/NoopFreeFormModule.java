package org.bbop.termgenie.services.freeform;

import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.process.ProcessState;


public class NoopFreeFormModule extends IOCModule {

	public NoopFreeFormModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(FreeFormTermService.class, NoopFreeFormTermServiceImpl.class);
	}

	
	public static class NoopFreeFormTermServiceImpl implements FreeFormTermService {

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public boolean canView(String sessionId, HttpSession session) {
			return false;
		}

		@Override
		public JsonFreeFormValidationResponse validate(String sessionId,
				JsonFreeFormTermRequest request,
				HttpSession session,
				ProcessState state)
		{
			return null;
		}
		
	}
}
