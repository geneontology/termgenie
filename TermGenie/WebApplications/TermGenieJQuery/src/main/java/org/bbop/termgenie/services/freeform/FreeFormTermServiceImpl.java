package org.bbop.termgenie.services.freeform;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.freeform.FreeFormTermValidator;
import org.bbop.termgenie.freeform.FreeFormValidationResponse;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.user.UserData;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class FreeFormTermServiceImpl implements FreeFormTermService {
	
	private static final Logger logger = Logger.getLogger(FreeFormTermServiceImpl.class);
	
	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final FreeFormTermValidator validator;
	private final Ontology ontology;
	private final MultiOntologyTaskManager manager;
	
	@Inject
	public FreeFormTermServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("CommitTargetOntology") OntologyTaskManager ontology,
			MultiOntologyTaskManager manager,
			FreeFormTermValidator validator)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.validator = validator;
		this.ontology = ontology.getOntology();
		this.manager = manager;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean canView(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowCommitReview = permissions.allowFreeForm(userData, ontology);
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonFreeFormValidationResponse validate(String sessionId,
			JsonFreeFormTermRequest request,
			HttpSession session,
			ProcessState state)
	{
		if (canView(sessionId, session)) {
			final FreeFormValidationResponse response = validator.validate(request, state);
			ConvertToJson task = new ConvertToJson(response);
			try {
				manager.runManagedTask(task, ontology);
			} catch (InvalidManagedInstanceException exception) {
				String message = "Error during term validation, due to an inconsistent ontology";
				logger.error(message, exception);
				return error(message);
			}
			return task.json;
		}
		return error("The user is not authorized, to use the free from termplate.");
	}
	
	
	private JsonFreeFormValidationResponse error(String message) {
		JsonFreeFormValidationResponse response = new JsonFreeFormValidationResponse();
		response.setGeneralError(message);
		return response;
	}

	static class ConvertToJson implements MultiResourceManagedTask<OWLGraphWrapper, Ontology> {
		
		final FreeFormValidationResponse response;
		JsonFreeFormValidationResponse json;
		
		/**
		 * @param response
		 */
		ConvertToJson(FreeFormValidationResponse response) {
			this.response = response;
		}

		@Override
		public List<Modified> run(List<OWLGraphWrapper> requested)
				throws InvalidManagedInstanceException
		{
			OWLGraphWrapper graph = requested.get(0);
			json = JsonFreeFormValidationResponse.convert(response, graph);
			return null;
		}
		
	}
}
