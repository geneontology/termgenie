package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.OntologyIdManager;
import org.bbop.termgenie.ontology.OntologyIdManager.OntologyIdManagerTask;
import org.bbop.termgenie.ontology.OntologyIdProvider;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.permissions.UserPermissions.CommitUserData;
import org.bbop.termgenie.tools.OntologyTools;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public abstract class AbstractTermCommitServiceImpl extends NoCommitTermCommitServiceImpl {

	private final InternalSessionHandler sessionHandler;
	private final Committer committer;
	private final OntologyIdManager idProvider;
	protected final UserPermissions permissions;

	protected AbstractTermCommitServiceImpl(OntologyTools ontologyTools,
			InternalSessionHandler sessionHandler,
			Committer committer,
			OntologyIdManager idProvider,
			UserPermissions permissions)
	{
		super(ontologyTools);
		this.sessionHandler = sessionHandler;
		this.committer = committer;
		this.idProvider = idProvider;
		this.permissions = permissions;
	}

	protected abstract Ontology getTargetOntology();

	protected abstract String getTempIdPrefix();

	@Override
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontologyName,
			HttpSession session)
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

		String guid = sessionHandler.getGUID(session);
		boolean allowCommit = permissions.allowCommit(guid, manager.getOntology());
		if (!allowCommit) {
			logger.warn("Insufficient rights for user attempt to commit. User: " + termgenieUser + " with GUID: " + guid);
			return error("Could not commit, the user is not authorized to execute a commit.");
		}
		
		CommitTask task = new CommitTask(manager, terms, termgenieUser, permissions.getCommitUserData(guid,
				manager.getOntology()));
		idProvider.runManagedTask(task);
		return task.result;
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
					existing.add(identifier+" "+label);
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
							sb.append(", ");
						}
						sb.append(matchingCommits.get(i));
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

		/**
		 * @param manager
		 * @param terms
		 * @param termgenieUser
		 * @param commitUserData
		 */
		CommitTask(OntologyTaskManager manager,
				JsonOntologyTerm[] terms,
				String termgenieUser,
				CommitUserData commitUserData)
		{
			super();
			this.manager = manager;
			this.terms = terms;
			this.termgenieUser = termgenieUser;
			this.commitUserData = commitUserData;
		}

		private final OntologyTaskManager manager;
		private final JsonOntologyTerm[] terms;
		private final String termgenieUser;
		private final CommitUserData commitUserData;

		private JsonCommitResult result = error("The commit operation is not enabled.");

		@Override
		protected void runSimple(OntologyIdProvider idProvider) {
			
			// check terms in the commit 
			CheckTermsTask checkTermsTask = new CheckTermsTask(terms);
			manager.runManagedTask(checkTermsTask);
			if (checkTermsTask.getException() != null) {
				result = error("Could not check terms due to an error: "+checkTermsTask.getException().getMessage());
				return;
			}
			else if (checkTermsTask.getMessage() != null) {
				result = error(checkTermsTask.getMessage());
				return;
			}
			
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

			CommitInfo commitInfo = createCommitInfo(commitTerms,
					termgenieUser,
					commitUserData);
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
				result.setTerms(createTerms(commitTerms));
				result.setSuccess(true);
				result.setDiff(commitResult.getDiff());

			} catch (CommitException exception) {
				if (exception.isRollback()) {
					idProvider.rollbackId(manager.getOntology(), base);
				}
				result = error(exception);
				return;
			}
			return;
		}

		private List<JsonOntologyTerm> createTerms(List<CommitObject<TermCommit>> commitTerms)
		{
			List<JsonOntologyTerm> terms = new ArrayList<JsonOntologyTerm>();
			for (CommitObject<TermCommit> commitObject : commitTerms) {
				if (commitObject.getType() == Modification.add) {
					terms.add(JsonOntologyTerm.createJson(commitObject.getObject().getTerm(), commitObject.getObject().getChanged()));
				}
			}
			return terms;
		}

		private Pair<List<CommitObject<TermCommit>>, Integer> createCommitTerms(JsonOntologyTerm[] terms,
				Ontology ontology,
				OntologyIdProvider idProvider) throws CommitException
		{
			List<CommitObject<TermCommit>> commits = new ArrayList<CommitObject<TermCommit>>();
			IdHandler idHandler = new IdHandler(idProvider, ontology, getTempIdPrefix());
			for (JsonOntologyTerm jsonTerm : terms) {
				Frame frame = JsonOntologyTerm.createFrame(jsonTerm, idHandler.create(jsonTerm.getTempId()));
				updateIdentifiers(frame, idHandler);
				List<Frame> changed = JsonOntologyTerm.createChangedFrames(jsonTerm.getChanged());
				if (changed != null && !changed.isEmpty()) {
					for (Frame changedFrame : changed) {
						updateIdentifiers(changedFrame, idHandler);
					}
				}
				TermCommit term = new TermCommit(frame, changed);
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
		
		
		private void updateIdentifiers(Frame frame, IdHandler idHandler) {
			updateIdentifiers(OBOConverterTools.getRelations(frame), idHandler);
		}
		
		private void updateIdentifiers(List<Clause> clauses, IdHandler idHandler) {
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
		}
	}

	/**
	 * Create {@link CommitInfo} instance for the given modifications.
	 * 
	 * @param terms
	 * @param relations
	 * @param termgenieUser
	 * @param commitUserData
	 * @return CommitInfo
	 */
	protected abstract CommitInfo createCommitInfo(List<CommitObject<TermCommit>> terms,
			String termgenieUser,
			CommitUserData commitUserData);

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
