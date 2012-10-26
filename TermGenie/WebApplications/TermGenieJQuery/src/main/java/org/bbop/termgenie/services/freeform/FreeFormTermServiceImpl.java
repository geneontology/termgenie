package org.bbop.termgenie.services.freeform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class FreeFormTermServiceImpl implements FreeFormTermService {
	
	private static final Logger logger = Logger.getLogger(FreeFormTermServiceImpl.class);
	
	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	
	private final Ontology ontology;
	private final MultiOntologyTaskManager manager;

	@Inject
	public FreeFormTermServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("CommitTargetOntology") OntologyTaskManager ontology,
			MultiOntologyTaskManager manager)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.ontology = ontology.getOntology();
		this.manager = manager;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
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
			return validateInternal(request);
		}
		return error("The user is not authorized, to use the free from termplate.");
	}
	
	
	private JsonFreeFormValidationResponse error(String message) {
		JsonFreeFormValidationResponse response = new JsonFreeFormValidationResponse();
		response.setGeneralError(message);
		return response;
	}
	
	private JsonFreeFormValidationResponse error(List<JsonFreeFormHint> errors) {
		JsonFreeFormValidationResponse response = new JsonFreeFormValidationResponse();
		response.setErrors(errors);
		return response;
	}
	
	private JsonFreeFormValidationResponse success(JsonOntologyTerm term) {
		JsonFreeFormValidationResponse response = new JsonFreeFormValidationResponse();
		response.setGeneratedTerm(term);
		return response;
	}

	
	private JsonFreeFormValidationResponse validateInternal(final JsonFreeFormTermRequest request) {
		
		FreeFormRequest task = new FreeFormRequest(request);
		try {
			manager.runManagedTask(task, ontology);
		} catch (InvalidManagedInstanceException exception) {
			String message = "Error during term validation, due to an inconsistent ontology";
			logger.error(message, exception);
			return error(message);
		}
		if (task.errors == null || task.errors.isEmpty()) {
			if (task.term == null) {
				return error("No term was generated from your request");
			}
			return success(task.term);
		}
		return error(task.errors);
	}


	static class FreeFormRequest implements MultiResourceManagedTask<OWLGraphWrapper, Ontology>
	{
	
		private final JsonFreeFormTermRequest request;
		private List<JsonFreeFormHint> errors = null;
		private JsonOntologyTerm term;
	
		private FreeFormRequest(JsonFreeFormTermRequest request) {
			this.request = request;
		}
	
		@Override
		public List<Modified> run(List<OWLGraphWrapper> requested)
				throws InvalidManagedInstanceException
		{
			OWLGraphWrapper graph = requested.get(0);
			runInternal(graph);
			return null; // no modifications
		}
		
		void runInternal(final OWLGraphWrapper graph) {
			// check label
			String requestedLabel = StringUtils.trimToNull(request.getLabel());
			if (requestedLabel == null) {
				setError("label", "A label is required for a free from request.");
				return;
			}
			requestedLabel = StringUtils.normalizeSpace(requestedLabel);
			
			// TODO discuss/implement a more clever check
			if (requestedLabel.length() < 10) {
				setError("label", "The provided label is too short.");
				return;
			}

			// search for similar labels and synonyms in the ontology 
			CharSequence normalizedLabel = normalizeLabel(requestedLabel);
			
			// check proposed synonyms at the same time 
			List<JsonSynonym> checkedSynonyms = null;
			Map<CharSequence, String> proposedSynonyms = null;
			
			// each synonym has to have:
			//  * label
			//  * scope (EXACT, NARROW, RELATED, No BROADER)
			// optional: xref
			List<JsonSynonym> jsonSynonyms = request.getSynonyms();
			if (jsonSynonyms != null && !jsonSynonyms.isEmpty()) {
				checkedSynonyms = new ArrayList<JsonSynonym>();
				proposedSynonyms = new HashMap<CharSequence, String>();
				Set<String> done = new HashSet<String>();
				for (JsonSynonym jsonSynonym : jsonSynonyms) {
					String synLabel = StringUtils.trimToNull(jsonSynonym.getLabel());
					if (synLabel == null) {
						addError("synonyms", "No empty labels as synonym allowed.");
						continue;
					}
					String lowerCase = synLabel.toLowerCase();
					if (done.contains(lowerCase)) {
						addError("synonyms", "Duplicate synonym: "+synLabel);
						continue;
					}
					String scope = StringUtils.trimToNull(jsonSynonym.getScope());
					if (scope == null) {
						scope = OboFormatTag.TAG_RELATED.getTag();
					}
					else {
						if (isOboScope(scope) == false) {
							addError("synonyms", "The synonym '"+synLabel+"' has an unknown scope: "+scope);
						}
					}
					checkedSynonyms.add(jsonSynonym);
					proposedSynonyms.put(normalizeLabel(synLabel), synLabel);
					done.add(lowerCase);
				}
			}
			
			
			for(OWLObject current : graph.getAllOWLObjects()) {
				String currentLabel = graph.getLabel(current);
				final CharSequence currentNormalizedLabel = normalizeLabel(currentLabel);
				if (currentLabel != null && similar(normalizedLabel, currentNormalizedLabel)) {
					setError("label", "The requested label is similar to the term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");
					return;
				}
				List<ISynonym> oboSynonyms = graph.getOBOSynonyms(current);
				for (ISynonym synonym : oboSynonyms) {
					String synLabel = synonym.getLabel();
					if (similar(normalizedLabel, normalizeLabel(synLabel))) {
						setError("label", "The requested label is similar to the synonym: '"+synLabel+"' of term: "+graph.getIdentifier(current)+" '"+currentLabel+"'");
						return;
					}
				}
			}
			// TODO check requested terms in the queue
			// TODO check blacklist

			// check namespace
			String namespace = StringUtils.trimToNull(request.getNamespace());
			if (namespace == null || namespace.isEmpty()) {
				setError("namespace", "A namespace is required for request: "+requestedLabel);
				return;
			}

			// check relations

			// at least one is_a parent in correct namespace
			final List<String> parents = request.getIsA();
			if (parents == null || parents.isEmpty()) {
				setError("parents", "At least one is_a parent required for request: "+requestedLabel);
				return;
			}
			Set<OWLClass> superClasses = new HashSet<OWLClass>();
			for (String parentId : parents) {
				OWLClass cls = graph.getOWLClassByIdentifier(parentId);
				if (cls == null) {
					addError("is_a parent", "parent not found in ontology: "+parentId);
					continue;
				}
				String parentNamespace = graph.getNamespace(cls);
				if (namespace.equals(parentNamespace)) {
					superClasses.add(cls);
				}
				else {
					addError("is_a parent", "namespace conflict parent namespace: '"+parentNamespace+"' requested namespace: '"+namespace+"'");
				}
			}
			if (errors != null) {
				return;
			}
			
			// TODO should we test for too high level parents? blacklist?
			
			// optional part_of relations
			Set<OWLClass> partOf = null;
			List<String> partOfList = request.getPartOf();			
			if (partOfList != null && !partOfList.isEmpty()) {
				partOf = new HashSet<OWLClass>();
				for(String id : partOfList) {
					OWLClass cls = graph.getOWLClassByIdentifier(id);
					if (cls == null) {
						addError("part_of parent", "parent not found in ontology: "+id);
						continue;
					}
					partOf.add(cls);
				}
			}
			if (errors != null) {
				return;
			}

			// check definition
			String def = StringUtils.trimToNull(request.getDefinition());
			if (def == null || def.length() < 20) {
				// check that the definition is at least X amount of chars long
				setError("definition", "Please enter a valid definition");
				return;
			}
			// search for similar definitions?
			
			// require at least on def db xref, ideally includes PMID
			List<String> xrefsList = request.getDbxrefs();
			if (xrefsList == null || xrefsList.isEmpty()) {
				setError("definition db xref", "Please enter at least one valid definition db xref");
				return;
			}
			Set<String> xrefs = new HashSet<String>();
			for(String  xref : xrefsList) {
				String dbxref = StringUtils.trimToNull(xref);
				if (dbxref != null) {
					continue;
				}
				// TODO validate xref
				// TODO find at least one PMID
				xrefs.add(dbxref);
			}
			
			if (xrefs.isEmpty()) {
				setError("definition db xref", "Please enter at least one valid definition db xref");
				return;
			}
			
			// all checks passed, create term
			
			// add relationships to owl and use reasoner to infer relationships and check satisfiability
			
			return;
		}
		
		void setError(String field, String messge) {
			errors = Collections.singletonList(new JsonFreeFormHint(field, messge));
		}
		
		void addError(String field, String message) {
			if (errors == null) {
				errors = new ArrayList<JsonFreeFormHint>();
			}
			errors.add(new JsonFreeFormHint(field, message));
		}
		
		static CharSequence normalizeLabel(CharSequence cs) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cs.length(); i++) {
				char c = cs.charAt(i);
				c = Character.toLowerCase(c);
				if (Character.isLetterOrDigit(c) == false) {
					c = '*';
				}
				sb.append(c);
			}
			return sb;
		}
		
		static boolean similar(CharSequence cs1, CharSequence cs2) {
			int distance = StringUtils.getLevenshteinDistance(cs1, cs2);
			// TODO make this threshold relative to the string length
			return distance <= 1;
		}
		
		static boolean isOboScope(String s) {
			return (OboFormatTag.TAG_RELATED.getTag().equals(s)) ||
					(OboFormatTag.TAG_NARROW.getTag().equals(s)) ||
					(OboFormatTag.TAG_EXACT.getTag().equals(s)) ||
					(OboFormatTag.TAG_BROAD.getTag().equals(s));
		}
	}
}
