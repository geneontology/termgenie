package org.bbop.termgenie.services.freeform;

import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonTermSuggestion;


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

		@Override
		public String[] getAvailableNamespaces(String sessionId, HttpSession session) {
			return new String[0];
		}

		@Override
		public JsonTermSuggestion[] autocomplete(String sessionId,
				String query,
				String oboNamespace,
				int max)
		{
			return null;
		}

		@Override
		public JsonCommitResult submit(String sessionId,
				JsonOntologyTerm term,
				boolean sendConfirmationEMail,
				HttpSession session,
				ProcessState processState)
		{
			return null;
		}

		@Override
		public String[] getXrefResources(String sessionId, HttpSession session) {
			return null;
		}
		
	}
}
