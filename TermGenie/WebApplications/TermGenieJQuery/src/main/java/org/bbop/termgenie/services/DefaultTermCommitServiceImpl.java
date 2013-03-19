package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.InternalCommitInfo;
import org.bbop.termgenie.ontology.OntologyIdManager;
import org.bbop.termgenie.ontology.OntologyIdManager.OntologyIdManagerTask;
import org.bbop.termgenie.ontology.OntologyIdProvider;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.services.freeform.InternalFreeFormCommitService;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.permissions.UserPermissions.CommitUserData;
import org.bbop.termgenie.tools.OntologyTools;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DefaultTermCommitServiceImpl extends NoCommitTermCommitServiceImpl implements InternalFreeFormCommitService {

	private final Committer committer;
	private final OntologyIdManager primaryIdProvider;
	private final OntologyIdManager secondaryIdProvider;
	protected final UserPermissions permissions;
	private final OntologyTaskManager source;
	private String tempIdPrefix;

	@Inject
	public DefaultTermCommitServiceImpl(OntologyTools ontologyTools,
			InternalSessionHandler sessionHandler,
			Committer committer,
			final @Named("CommitTargetOntology") OntologyTaskManager source,
			final @Named("PrimaryOntologyIdManager") OntologyIdManager primaryIdProvider,
			final @Named("SecondaryOntologyIdManager") OntologyIdManager secondaryIdProvider,
			final TermGenerationEngine generationEngine,
			UserPermissions permissions)
	{
		super(ontologyTools, sessionHandler);
		this.committer = committer;
		this.primaryIdProvider = primaryIdProvider;
		this.secondaryIdProvider = secondaryIdProvider;
		this.permissions = permissions;
		this.source = source;
		try {
			source.runManagedTask(new OntologyTask() {

				@Override
				protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
					tempIdPrefix = generationEngine.getTempIdPrefix(managed);
				}
			});
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected Ontology getTargetOntology() {
		return source.getOntology();
	}

	/**
	 * Create {@link CommitInfo} instance for the given modifications. Overwrite
	 * this method for more complex commitInfo
	 * 
	 * @param terms
	 * @param userData
	 * @param commitMessage
	 * @param commitUserData
	 * @param sendConfirmationEMail
	 * @return CommitInfo
	 */
	protected CommitInfo createCommitInfo(List<CommitObject<TermCommit>> terms,
			String commitMessage,
			UserData userData,
			CommitUserData commitUserData,
			boolean sendConfirmationEMail)
	{
		return new InternalCommitInfo(terms, commitMessage, userData, sendConfirmationEMail);
	}

	@Override
	public JsonCommitResult commitFreeFormTerms(String sessionId,
			JsonOntologyTerm[] terms,
			OntologyTaskManager manager,
			boolean sendConfirmationEMail,
			String tempIdPrefix,
			HttpSession session,
			ProcessState processState)
	{
		// check if session is valid, get user name
		boolean validSession = sessionHandler.isValidSession(sessionId, session);
		if (!validSession) {
			return error("Could not commit as the session is not valid.");
		}

		String termgenieUser = sessionHandler.isAuthenticated(sessionId, session);
		if (termgenieUser == null) {
			return error("Could not commit as the user is not logged.");
		}

		UserData userData = sessionHandler.getUserData(session);
		boolean allowCommit = permissions.allowFreeFormCommit(userData, manager.getOntology());
		if (!allowCommit) {
			logger.warn("Insufficient rights for user attempt to a free from term commit. User: " + termgenieUser + " with GUID: " + userData.getGuid());
			return error("Could not commit, the user is not authorized to execute a free form term commit.");
		}
		
		String commitMessage = createDefaultCommitMessage(userData);
		CommitTask task = new CommitTask(manager, terms, commitMessage, userData, permissions.getCommitUserData(userData,
				manager.getOntology()), sendConfirmationEMail, tempIdPrefix, processState);
		try {
			secondaryIdProvider.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Could not commit term due to an invalid ontology state", exception);
			return error("Could not commit term due to an invalid ontology state: "+exception.getMessage());
		}
		return task.result;
	}

	@Override
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontologyName,
			boolean sendConfirmationEMail,
			HttpSession session,
			ProcessState processState)
	{
		// check if its the correct ontology
		OntologyTaskManager manager = getOntologyManager(ontologyName);
		if (manager == null) {
			return error("Unknown ontology: " + ontologyName);
		}
		if (!getTargetOntology().getUniqueName().equals(manager.getOntology().getUniqueName())) {
			return error("Can only commit to " + getTargetOntology().getUniqueName() + ", but requested ontology was: " + ontologyName);
		}

		// check if session is valid, get user name
		boolean validSession = sessionHandler.isValidSession(sessionId, session);
		if (!validSession) {
			return error("Could not commit as the session is not valid.");
		}

		String termgenieUser = sessionHandler.isAuthenticated(sessionId, session);
		if (termgenieUser == null) {
			return error("Could not commit as the user is not logged.");
		}

		UserData userData = sessionHandler.getUserData(session);
		boolean allowCommit = permissions.allowCommit(userData, manager.getOntology());
		if (!allowCommit) {
			logger.warn("Insufficient rights for user attempt to commit. User: " + termgenieUser + " with GUID: " + userData.getGuid());
			return error("Could not commit, the user is not authorized to execute a commit.");
		}

		String commitMessage = createDefaultCommitMessage(userData);
		CommitTask task = new CommitTask(manager, terms, commitMessage, userData, permissions.getCommitUserData(userData,
				manager.getOntology()), sendConfirmationEMail, tempIdPrefix, processState);
		try {
			primaryIdProvider.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Could not commit term due to an invalid ontology state", exception);
			return error("Could not commit term due to an invalid ontology state: "+exception.getMessage());
		}
		return task.result;
	}

	private String createDefaultCommitMessage(UserData userData) {
		String name = userData.getScmAlias();
		if (name == null) {
			name = userData.getScreenname();
		}
		if (name == null) {
			name = userData.getEmail();
		}
		return "TermGenie commit for user: " + name;
	}

	private class CheckTermsTask extends OntologyTask {

		private final JsonOntologyTerm[] terms;

		/**
		 * @param terms
		 */
		CheckTermsTask(JsonOntologyTerm[] terms) {
			super();
			this.terms = terms;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			int missingRelations = 0;
			List<String> existing = new ArrayList<String>();
			List<String> labels = new ArrayList<String>(terms.length);
			for (JsonOntologyTerm term : terms) {
				List<String> relations = term.getRelations();
				if (relations == null || relations.isEmpty()) {
					missingRelations += 1;
				}
				String label = term.getLabel();
				labels.add(label);
				OWLObject owlObject = managed.getOWLObjectByLabel(label);
				if (owlObject != null) {
					String identifier = managed.getIdentifier(owlObject);
					existing.add(identifier + " " + label);
				}
			}
			if (!existing.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Could not commit, ");
				if (existing.size() == 1) {
					sb.append("a term with the same label already exists: ");
					sb.append(existing.get(0));
				}
				else {
					sb.append("the following terms with the same label already exist: ");
					for (int i = 0; i < existing.size(); i++) {
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(existing.get(i));
					}
				}
				setMessage(sb.toString());
				return;
			}
			if (missingRelations > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Could not commit, there ");
				if (missingRelations == 1) {
					sb.append("is one term with no relations.");
				}
				else {
					sb.append("are ");
					sb.append(missingRelations);
					sb.append(" terms with no relations.");
				}
				setMessage(sb.toString());
				return;
			}
			List<Pair<String, String>> matchingCommits = committer.checkRecentCommits(labels);
			if (matchingCommits != null && !matchingCommits.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Could not commit, ");
				if (existing.size() == 1) {
					sb.append("a term with the same label was recently committed: ");
					sb.append(matchingCommits.get(0));
				}
				else {
					sb.append("the following terms with the same label were recently committed: ");
					for (int i = 0; i < matchingCommits.size(); i++) {
						if (i > 0) {
							sb.append(',');
						}
						sb.append('\n');
						Pair<String, String> pair = matchingCommits.get(i);
						String id = pair.getOne();
						String label = pair.getTwo();
						sb.append(id).append(" with Label: '").append(label).append('\'');
					}
				}
				setMessage(sb.toString());
				return;
			}
		}

	}

	/**
	 * <p>
	 * This task encapsulates the commit operation and the required creation of
	 * valid ontology identifiers
	 * </p>
	 * <p>
	 * Using a {@link ManagedTask} has the purpose of guaranteeing that only one
	 * thread is actively creating new identifiers to allow a secure roll-back
	 * of unused identifiers in the case of an error.
	 * </p>
	 */
	private class CommitTask extends OntologyIdManagerTask {

		private final String commitMessage;
		private final ProcessState processState;
		private final boolean sendConfirmationEMail;
		private final String tempIdPrefix;

		/**
		 * @param manager
		 * @param terms
		 * @param commitMessage
		 * @param userData
		 * @param commitUserData
		 * @param sendConfirmationEMail
		 * @param processState
		 */
		CommitTask(OntologyTaskManager manager,
				JsonOntologyTerm[] terms,
				String commitMessage,
				UserData userData,
				CommitUserData commitUserData,
				boolean sendConfirmationEMail,
				String tempIdPrefix,
				ProcessState processState)
		{
			super();
			this.manager = manager;
			this.terms = terms;
			this.commitMessage = commitMessage;
			this.userData = userData;
			this.commitUserData = commitUserData;
			this.sendConfirmationEMail = sendConfirmationEMail;
			this.tempIdPrefix = tempIdPrefix;
			this.processState = processState;
		}

		private final OntologyTaskManager manager;
		private final JsonOntologyTerm[] terms;
		private final UserData userData;
		private final CommitUserData commitUserData;

		private JsonCommitResult result = error("The commit operation is not enabled.");

		@Override
		protected void runSimple(OntologyIdProvider idProvider) {

			ProcessState.addMessage(processState, "Verifying terms for commit.");
			
			// check terms in the commit
			CheckTermsTask checkTermsTask = new CheckTermsTask(terms);
			try {
				manager.runManagedTask(checkTermsTask);
			} catch (InvalidManagedInstanceException exception) {
				logger.error("Could not check terms due to an error", exception);
				result = error("Could not check terms due to an error: " + checkTermsTask.getException().getMessage());
				return;
			}
			if (checkTermsTask.getException() != null) {
				result = error("Could not check terms due to an error: " + checkTermsTask.getException().getMessage());
				return;
			}
			else if (checkTermsTask.getMessage() != null) {
				result = error(checkTermsTask.getMessage());
				return;
			}

			ProcessState.addMessage(processState, "Generating permanent identifier for terms.");
			// create terms with new termIds
			Pair<List<CommitObject<TermCommit>>, Integer> pair;
			try {
				pair = createCommitTerms(terms, manager.getOntology(), idProvider);
			} catch (CommitException exception) {
				result = error(exception.getMessage());
				return;
			}
			Integer base = pair.getTwo();
			List<CommitObject<TermCommit>> commitTerms = pair.getOne();

			// update "Generated_by" tag with username
			for (CommitObject<TermCommit> commitObject : commitTerms) {
				if (Modification.add == commitObject.getType()) {
					TermCommit termCommit = commitObject.getObject();
					Frame frame = termCommit.getTerm();
					OboTools.updateClauseValues(frame,
							OboFormatTag.TAG_CREATED_BY,
							userData.getScmAlias());
				}
			}

			ProcessState.addMessage(processState, "Start - Writing terms to internal database for review.");
			CommitInfo commitInfo = createCommitInfo(commitTerms,
					commitMessage,
					userData,
					commitUserData,
					sendConfirmationEMail);
			try {
				// commit
				CommitResult commitResult = committer.commit(commitInfo);

				// check commit status
				if (!commitResult.isSuccess()) {
					error("Commit operation did not succeed.");
				}

				// create result
				result = new JsonCommitResult();
				String message = commitResult.getMessage();
				if (message == null) {
					message = "Commit operation finished successfully.";
				}
				result.setMessage(message);
				CreateJsonTermsTask task = new CreateJsonTermsTask(commitTerms);
				manager.runManagedTask(task);
				result.setTerms(task.terms);
				result.setSuccess(true);
				result.setDiff(commitResult.getDiff());

			} catch (CommitException exception) {
				if (exception.isRollback()) {
					idProvider.rollbackId(manager.getOntology(), base);
				}
				result = error(exception);
				return;
			} catch (InvalidManagedInstanceException exception) {
				logger.error("Could not create data for successfull commit result", exception);
				result = error("Commit successfull, but could not create data for successfull commit result display: "+exception.getMessage());
				return;
			}
			return;
		}

		private class CreateJsonTermsTask extends OntologyTask {

			private final List<CommitObject<TermCommit>> commitTerms;
			private List<JsonOntologyTerm> terms;

			/**
			 * @param commitTerms
			 */
			CreateJsonTermsTask(List<CommitObject<TermCommit>> commitTerms) {
				super();
				this.commitTerms = commitTerms;
			}

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
				terms = new ArrayList<JsonOntologyTerm>();
				for (CommitObject<TermCommit> commitObject : commitTerms) {
					if (commitObject.getType() == Modification.add) {
						TermCommit object = commitObject.getObject();
						terms.add(JsonOntologyTerm.createJson(object.getTerm(), object.getOwlAxioms(), object.getChanged(), managed, object.getPattern()));
					}
				}
			}
		}

		private Pair<List<CommitObject<TermCommit>>, Integer> createCommitTerms(JsonOntologyTerm[] terms,
				Ontology ontology,
				OntologyIdProvider idProvider) throws CommitException
		{
			List<CommitObject<TermCommit>> commits = new ArrayList<CommitObject<TermCommit>>();
			IdHandler idHandler = new IdHandler(idProvider, ontology, tempIdPrefix);
			for (JsonOntologyTerm jsonTerm : terms) {
				Frame frame = JsonOntologyTerm.createFrame(jsonTerm,
						idHandler.create(jsonTerm.getTempId()));
				Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(jsonTerm.getOwlAxioms());
				axioms = updateIdentifiers(frame, axioms, idHandler);
				List<Pair<Frame, Set<OWLAxiom>>> changed = JsonOntologyTerm.createChangedFrames(jsonTerm.getChanged());
				if (changed != null && !changed.isEmpty()) {
					for (Pair<Frame, Set<OWLAxiom>> pair : changed) {
						pair.setTwo(updateIdentifiers(pair.getOne(), pair.getTwo(), idHandler));
					}
				}
				TermCommit term = new TermCommit(frame, axioms, changed, jsonTerm.getPattern());
				commits.add(CommitObject.add(term));
			}

			// Check that all mapped temporary ids are also created!
			Collection<String> missingIds = idHandler.getMissingIds();
			if (!missingIds.isEmpty()) {
				// rollback
				idProvider.rollbackId(ontology, idHandler.base);
				throw new CommitException("Missing terms with tempIds: " + missingIds, true);
			}
			return new Pair<List<CommitObject<TermCommit>>, Integer>(commits, idHandler.base);
		}

		private Set<OWLAxiom> updateIdentifiers(Frame frame, Set<OWLAxiom> axioms, IdHandler idHandler) {
			return updateIdentifiers(OboTools.getRelations(frame), axioms, idHandler);
		}

		private Set<OWLAxiom> updateIdentifiers(List<Clause> clauses, Set<OWLAxiom> axioms, IdHandler idHandler) {
			if (clauses != null && !clauses.isEmpty()) {
				for (Clause clause : clauses) {
					Collection<Object> values = clause.getValues();
					if (values != null) {
						List<Object> list;
						if (values instanceof List) {
							list = (List<Object>) values;
						}
						else {
							list = new ArrayList<Object>(values);
							clause.setValues(list);
						}
						for (int i = 0; i < list.size(); i++) {
							Object object = list.get(i);
							if (object != null && object instanceof String) {
								String value = (String) object;
								list.set(i, idHandler.map(value));
							}
						}
					}
				}
			}
			Map<IRI, IRI> replacements = idHandler.createIRIMap();
			return OwlStringTools.replace(axioms, replacements);
		}
	}

	// ---------------- Helper ----------------

	/**
	 * Helper to handle the mapping of temporary identifiers to ontology
	 * specific new identifiers. Also used to check that all identifiers used in
	 * Relations are mapped.
	 */
	static class IdHandler {

		private final OntologyIdProvider idProvider;
		private final Ontology ontology;
		private final String tempIdPrefix;

		private Integer base;
		private Map<String, String> tempIdMap;
		private Set<String> used;

		private boolean isTempId(String s) {
			return s.toLowerCase().startsWith(tempIdPrefix);
		}
		
		/**
		 * @param idProvider provider of valid/permanent identifiers
		 * @param ontology targetOntology
		 * @param tempIdPrefix unique prefix to identify, temporary ids
		 */
		IdHandler(OntologyIdProvider idProvider, Ontology ontology, String tempIdPrefix) {
			super();
			this.idProvider = idProvider;
			this.ontology = ontology;
			this.tempIdPrefix = Owl2Obo.getIdentifier(IRI.create(tempIdPrefix.toLowerCase())).toLowerCase();
			tempIdMap = new HashMap<String, String>();
			used = new HashSet<String>();
			base = null;
		}

		/**
		 * Map temporary identifiers into a valid one. Does not modify valid
		 * identifiers. Use this method to translate identifiers in
		 * relationships.
		 * 
		 * @param tempId identifier
		 * @return valid identifier
		 * @see #create(String)
		 */
		String map(String tempId) {
			if (!isTempId(tempId)) {
				return tempId;
			}
			String id = tempIdMap.get(tempId);
			if (id == null) {
				Pair<String, Integer> pair = idProvider.getNewId(ontology);
				if (base == null) {
					base = pair.getTwo();
				}
				id = pair.getOne();
				tempIdMap.put(tempId, id);
			}
			return id;
		}
		
		/**
		 * Map temporary identifiers into a valid one. Does not modify valid
		 * identifiers. Additionally, flags the tempId as created.
		 * 
		 * @param tempId identifier
		 * @return valid identifier
		 * @see #map(String)
		 */
		String create(String tempId) {
			if (!isTempId(tempId)) {
				return tempId;
			}
			String id = map(tempId);
			used.add(tempId);
			return id;
		}

		/**
		 * Retrieve the {@link Collection} of identifiers, which have been used
		 * in {@link #map(String)}, but have not been marked in
		 * {@link #create(String)}.
		 * 
		 * @return collection of missing Identifiers.
		 */
		Collection<String> getMissingIds() {
			Set<String> missingIds = new HashSet<String>(tempIdMap.keySet());
			missingIds.removeAll(used);
			return missingIds;
		}
		
		/**
		 * Create a map of temporary IRIs and their new permanent IRIs.
		 * 
		 * @return map with temporary IRI mapping
		 */
		Map<IRI, IRI> createIRIMap() {
			final Obo2Owl obo2Owl = new Obo2Owl();
			obo2Owl.setObodoc(new OBODoc());
			Map<IRI, IRI> map = new HashMap<IRI, IRI>();
			for(Entry<String, String> entry : tempIdMap.entrySet()) {
				IRI keyIRI = obo2Owl.oboIdToIRI(entry.getKey());
				IRI valueIRI = obo2Owl.oboIdToIRI(entry.getValue());
				map.put(keyIRI, valueIRI);
			}
			return map ;
		}
	}

	private static JsonCommitResult error(String message) {
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage(message);
		return result;
	}

	private static JsonCommitResult error(CommitException exception) {
		StringBuilder message = new StringBuilder("The commit operation did not succeed, exception: ");
		message.append(exception.getMessage());
		if (exception.getCause() != null) {
			message.append("\nCause: ");
			message.append(exception.getCause().getMessage());
		}
		return error(message.toString());
	}
}
