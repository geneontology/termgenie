package org.bbop.termgenie.services.freeform;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.freeform.FreeFormTermValidator;
import org.bbop.termgenie.freeform.FreeFormValidationResponse;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.TermCommitService;
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
	private final OntologyTaskManager targetOntology;
	private final MultiOntologyTaskManager manager;
	private final OntologyTermSuggestor suggestor;
	private final InternalFreeFormCommitService commitService;
	private List<String> xrefResources = null;
	
	@Inject
	public FreeFormTermServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("CommitTargetOntology") OntologyTaskManager ontology,
			OntologyTermSuggestor suggestor,
			MultiOntologyTaskManager manager,
			TermCommitService commitService,
			FreeFormTermValidator validator)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.suggestor = suggestor;
		this.validator = validator;
		this.targetOntology = ontology;
		this.ontology = ontology.getOntology();
		this.manager = manager;
		if (commitService instanceof InternalFreeFormCommitService) {
			this.commitService = (InternalFreeFormCommitService) commitService;
		}
		else {
			this.commitService = null;
		}
	}
	
	
	/**
	 * @param xrefResources the xrefResources to set
	 */
	@Inject(optional=true)
	public void setXrefResources(@Named("FreeFormXrefResources") List<String> xrefResources) {
		this.xrefResources = xrefResources;
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
	public String[] getAvailableNamespaces(String sessionId, HttpSession session) {
		if (canView(sessionId, session)) {
			List<String> namespaces = validator.getOboNamespaces();
			if (namespaces != null && !namespaces.isEmpty()) {
				return namespaces.toArray(new String[namespaces.size()]);
			}
		}
		return null;
	}

	@Override
	public String[] getXrefResources(String sessionId, HttpSession session) {
		if (canView(sessionId, session)) {
			if (xrefResources != null && !xrefResources.isEmpty()) {
			String[] array = xrefResources.toArray(new String[xrefResources.size()]);
			return array;
			}
		}
		return null;
	}

	@Override
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String oboNamespace,
			int max)
	{
		// sanity checks
		if (query == null || query.length() <= 2) {
			return null;
		}
		if (max < 0 || max > 10) {
			max = 10;
		}
		Ontology ontology;
		if (oboNamespace == null || oboNamespace.isEmpty()) {
			ontology = this.ontology;
		}
		else {
			ontology = new Ontology(this.ontology.getUniqueName(), oboNamespace, this.ontology.getRoots()) {
				// intentionally empty
			};
		}
		// query for terms
		List<TermSuggestion> autocompleteList = suggestor.suggestTerms(query, ontology, max);
		if (autocompleteList != null && !autocompleteList.isEmpty()) {
			JsonTermSuggestion[] result = new JsonTermSuggestion[autocompleteList.size()];
			for (int i = 0; i < result.length; i++) {
				TermSuggestion termSuggestion = autocompleteList.get(i);
				JsonOntologyTermIdentifier jsonId = new JsonOntologyTermIdentifier(ontology.getUniqueName(), termSuggestion.getIdentifier());
				result[i] = new JsonTermSuggestion(termSuggestion.getLabel(), jsonId , termSuggestion.getDescription(), termSuggestion.getSynonyms());
			}
			return result;
		}
		return null;
	}

	@Override
	public JsonFreeFormValidationResponse validate(String sessionId,
			JsonFreeFormTermRequest request,
			HttpSession session,
			ProcessState state)
	{
		if (canView(sessionId, session)) {
			boolean requireLiteratureReference = requireLiteratureReference(sessionId, session);
			final FreeFormValidationResponse response = validator.validate(request, requireLiteratureReference, state);
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
	
	private boolean requireLiteratureReference(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowFreeFormLiteratureXrefOptional = permissions.allowFreeFormLiteratureXrefOptional(userData, ontology);
				return allowFreeFormLiteratureXrefOptional == false;
			}
		}
		return false;
	}
	
	@Override
	public JsonCommitResult submit(String sessionId,
			JsonOntologyTerm term,
			boolean sendConfirmationEMail,
			HttpSession session,
			ProcessState processState)
	{
		if (commitService != null) {
			JsonOntologyTerm[] terms = new JsonOntologyTerm[] { term };
			JsonCommitResult result = commitService.commitFreeFormTerms(sessionId,
					terms,
					targetOntology,
					sendConfirmationEMail,
					validator.getTempIdPrefix(),
					session,
					processState);
			return result;
		}
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("Internal error: No commit possible, as no apropriate service is available.");
		return result;
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
