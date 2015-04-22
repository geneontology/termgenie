package org.bbop.termgenie.services.freeform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;
import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.freeform.FreeFormTermValidator;
import org.bbop.termgenie.freeform.FreeFormValidationResponse;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.permissions.UserPermissions;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.user.OrcidUserData;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataProvider;
import org.bbop.termgenie.user.XrefUserData;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class FreeFormTermServiceImpl implements FreeFormTermService {
	
	private static final Logger logger = Logger.getLogger(FreeFormTermServiceImpl.class);
	
	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final UserDataProvider userDataProvider;
	private final FreeFormTermValidator validator;
	private final Ontology ontology;
	private final OntologyTaskManager targetOntology;
	private final OntologyTermSuggestor suggestor;
	private final InternalFreeFormCommitService commitService;
	
	private boolean doAsciiCheck = false;

	private final String defaultSubset;
	
	@Inject
	public FreeFormTermServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			UserDataProvider userDataProvider,
			OntologyLoader loader,
			OntologyTermSuggestor suggestor,
			@Named("FreeFormAutocompleteDefaultSubset") String defaultSubset,
			TermCommitService commitService,
			FreeFormTermValidator validator)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.userDataProvider = userDataProvider;
		this.suggestor = suggestor;
		this.defaultSubset = defaultSubset;
		this.validator = validator;
		this.targetOntology = loader.getOntologyManager();
		this.ontology = targetOntology.getOntology();
		if (commitService instanceof InternalFreeFormCommitService) {
			this.commitService = (InternalFreeFormCommitService) commitService;
		}
		else {
			this.commitService = null;
		}
	}
	
	
	/**
	 * @param doAsciiCheck the doAsciiCheck to set
	 */
	@Inject(optional=true)
	public void setDoAsciiCheck(@Named("FreeFormDoAsciiCheck") @Nullable Boolean doAsciiCheck) {
		if (doAsciiCheck != null) {
			this.doAsciiCheck = doAsciiCheck.booleanValue();
		}
		else {
			this.doAsciiCheck = false;
		}
	}

	@Override
	public JsonFreeFormConfig getConfig() {
		JsonFreeFormConfig config = new JsonFreeFormConfig(true);
		List<String> namespaces = validator.getOboNamespaces();
		if (namespaces != null && !namespaces.isEmpty()) {
			config.setOboNamespaces(namespaces.toArray(new String[namespaces.size()]));
		}
		List<String> relations = validator.getAdditionalRelations();
		if (relations != null && !relations.isEmpty()) {
			config.setAdditionalRelations(relations.toArray(new String[relations.size()]));
		}
		config.setDoAsciiCheck(doAsciiCheck);
		return config;
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
	public AutoCompleteEntry[] getAutoCompleteResource(String sessionId,
			String resource,
			HttpSession session)
	{
		if (canView(sessionId, session)) {
			if ("xref".equals(resource)) {
				List<XrefUserData> userData = userDataProvider.getXrefUserData();
				List<AutoCompleteEntry> xrefValues = new ArrayList<AutoCompleteEntry>();
				Set<String> xrefs = new HashSet<String>();
				if (userData != null && !userData.isEmpty()) {
					for (XrefUserData xrefUserData : userData) {
						String name = xrefUserData.getScreenname();
						String value = xrefUserData.getXref();
						if (value != null) {
							xrefs.add(value);
							AutoCompleteEntry entry = new AutoCompleteEntry();
							entry.setName(name);
							entry.setValue(value);
							xrefValues.add(entry);
						}
					}
				}
				Set<String> additionalXrefs = userDataProvider.getAdditionalXrefs();
				if (additionalXrefs != null && !additionalXrefs.isEmpty()) {
					for (String xref : additionalXrefs) {
						if (!xrefs.contains(xref)) {
							// skip existing
							AutoCompleteEntry entry = new AutoCompleteEntry();
							entry.setValue(xref);
							xrefValues.add(entry);
						}
					}
				}
				if (!xrefValues.isEmpty()) {
					AutoCompleteEntry[] array = xrefValues.toArray(new AutoCompleteEntry[xrefValues.size()]);
					return array;
				}
			}
			else if ("orcid".equals(resource)) {
				List<OrcidUserData> userData = userDataProvider.getOrcIdUserData();
				if (userData != null && !userData.isEmpty()) {
					List<AutoCompleteEntry> xrefStrings = new ArrayList<AutoCompleteEntry>(userData.size());
					for (OrcidUserData orcid : userData) {
						String name = orcid.getScreenname();
						String value = orcid.getOrcid();
						if (value != null) {
							AutoCompleteEntry entry = new AutoCompleteEntry();
							entry.setName(name);
							entry.setValue(value);
							xrefStrings.add(entry);
						}
					}
					if (!xrefStrings.isEmpty()) {
						AutoCompleteEntry[] array = xrefStrings.toArray(new AutoCompleteEntry[xrefStrings.size()]);
						return array;
					}
				}
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
		String ontology;
		if (oboNamespace == null || oboNamespace.isEmpty()) {
			ontology = defaultSubset;
		}
		else {
			ontology = oboNamespace;
		}
		// query for terms
		List<TermSuggestion> autocompleteList = suggestor.suggestTerms(query, ontology, max);
		if (autocompleteList != null && !autocompleteList.isEmpty()) {
			JsonTermSuggestion[] result = new JsonTermSuggestion[autocompleteList.size()];
			for (int i = 0; i < result.length; i++) {
				TermSuggestion termSuggestion = autocompleteList.get(i);
				JsonOntologyTermIdentifier jsonId = new JsonOntologyTermIdentifier(ontology, termSuggestion.getIdentifier());
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
			boolean isEditor = isEditor(sessionId, session);
			final FreeFormValidationResponse response = validator.validate(request, isEditor, state);
			ConvertToJson task = new ConvertToJson(response);
			try {
				targetOntology.runManagedTask(task);
			} catch (InvalidManagedInstanceException exception) {
				String message = "Error during term validation, due to an inconsistent ontology";
				logger.error(message, exception);
				return error(message);
			}
			return task.json;
		}
		return error("The user is not authorized, to use the free from termplate.");
	}
	
	private boolean isEditor(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean isEditor = permissions.allowFreeFormLiteratureXrefOptional(userData, ontology);
				return isEditor;
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

	static class ConvertToJson extends OntologyTask {
		
		final FreeFormValidationResponse response;
		JsonFreeFormValidationResponse json;
		
		/**
		 * @param response
		 */
		ConvertToJson(FreeFormValidationResponse response) {
			this.response = response;
		}

		@Override
		protected void runCatching(OWLGraphWrapper graph) throws TaskException, Exception {
			json = JsonFreeFormValidationResponse.convert(response, graph);
		}
		
	}
}
