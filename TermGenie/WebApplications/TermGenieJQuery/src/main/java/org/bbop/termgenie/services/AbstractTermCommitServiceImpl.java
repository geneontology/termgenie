package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.OntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermMetaData;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermRelation;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.OntologyIdManager;
import org.bbop.termgenie.ontology.OntologyIdManager.OntologyIdManagerTask;
import org.bbop.termgenie.ontology.OntologyIdProvider;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.tools.OntologyTools;
import org.bbop.termgenie.tools.Pair;

import owltools.graph.OWLGraphWrapper.Synonym;

public abstract class AbstractTermCommitServiceImpl extends NoCommitTermCommitServiceImpl {

	private final SessionHandler sessionHandler;
	private final Committer committer;
	private final OntologyIdManager idProvider;

	protected AbstractTermCommitServiceImpl(OntologyTools ontologyTools,
			SessionHandler sessionHandler,
			Committer committer,
			OntologyIdManager idProvider)
	{
		super(ontologyTools);
		this.sessionHandler = sessionHandler;
		this.committer = committer;
		this.idProvider = idProvider;
	}

	protected abstract Ontology getTargetOntology();

	protected abstract String getTempIdPrefix();

	@Override
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontologyName)
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
		boolean validSession = sessionHandler.isValidSession(sessionId);
		if (!validSession) {
			return error("Could not commit as the session is not valid.");
		}

		boolean isAuthenticated = sessionHandler.isAuthenticated(sessionId);
		if (!isAuthenticated) {
			return error("Could not commit as the user is not logged.");
		}

		String termgenieUser = sessionHandler.getValue(sessionId, "TermGenieUserName");
		if (termgenieUser == null) {
			// this should never happen, as a valid user name is required for
			// authentication
			return error("Internal error. The session has no valid username.");
		}

		// TODO check if user has permissions for trying a commit

		CommitTask task = new CommitTask(manager, terms, termgenieUser);
		this.idProvider.runManagedTask(task);
		return task.result;
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
		 */
		CommitTask(OntologyTaskManager manager, JsonOntologyTerm[] terms, String termgenieUser) {
			super();
			this.manager = manager;
			this.terms = terms;
			this.termgenieUser = termgenieUser;
		}

		private final OntologyTaskManager manager;
		private final JsonOntologyTerm[] terms;
		private final String termgenieUser;

		private JsonCommitResult result = error("The commit operation is not enabled.");

		@Override
		protected void runSimple(OntologyIdProvider idProvider) {
			// create terms with new termIds
			Pair<List<CommitObject<OntologyTerm>>, Integer> pair;
			try {
				pair = createCommitTerms(terms, manager.getOntology(), idProvider);
			} catch (CommitException exception) {
				error(exception.getMessage());
				return;
			}
			Integer base = pair.getTwo();
			List<CommitObject<OntologyTerm>> commitTerms = pair.getOne();

			CommitInfo commitInfo = createCommitInfo(commitTerms, null, termgenieUser);
			try {
				// commit
				CommitResult commitResult = committer.commit(commitInfo);

				// check commit status
				if (!commitResult.isSuccess()) {
					error("Commit operation did not succeed.");
				}

				// create result
				result = new JsonCommitResult();
				result.setMessage("Commit operation finished successfully.");
				result.setTerms(terms);
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

		private Pair<List<CommitObject<OntologyTerm>>, Integer> createCommitTerms(JsonOntologyTerm[] terms,
				Ontology ontology,
				OntologyIdProvider idProvider) throws CommitException
		{
			List<CommitObject<OntologyTerm>> commits = new ArrayList<CommitObject<OntologyTerm>>();
			IdHandler idHandler = new IdHandler(idProvider, ontology, getTempIdPrefix());
			for (JsonOntologyTerm jsonTerm : terms) {
				String id = idHandler.create(jsonTerm.getTempId());
				String label = jsonTerm.getLabel();
				String definition = jsonTerm.getDefinition();
				List<Synonym> synonyms = extractSynonyms(jsonTerm);
				List<String> defXRef = extractDefXRef(jsonTerm);
				Map<String, String> metaData = extractMetaData(jsonTerm);
				List<IRelation> relations = extractRelations(jsonTerm, idHandler);
		
				OntologyTerm term = new DefaultOntologyTerm(id, label, definition, synonyms, defXRef, metaData, relations);
		
				commits.add(CommitObject.add(term));
			}
		
			// Check that all mapped temporary ids are also created!
			Collection<String> missingIds = idHandler.getMissingIds();
			if (!missingIds.isEmpty()) {
				// rollback
				idProvider.rollbackId(ontology, idHandler.base);
				throw new CommitException("Missing terms with tempIds: " + missingIds, true);
			}
			return new Pair<List<CommitObject<OntologyTerm>>, Integer>(commits, idHandler.base);
		}

		private List<IRelation> extractRelations(JsonOntologyTerm jsonTerm, IdHandler idHandler) {
			JsonTermRelation[] relations = jsonTerm.getRelations();
			if (relations != null) {
				List<IRelation> result = new ArrayList<IRelation>(relations.length);
				for (JsonTermRelation jsonTermRelation : relations) {
					String source = idHandler.map(jsonTermRelation.getSource());
					String target = idHandler.map(jsonTermRelation.getTarget());
					String targetLabel = jsonTermRelation.getTargetLabel();
					Map<String, String> properties = new HashMap<String, String>();
					String[][] jsonProperties = jsonTermRelation.getProperties();
					if (jsonProperties != null && jsonProperties.length > 0) {
						for (String[] pair : jsonProperties) {
							if (pair != null && pair.length == 2) {
								properties.put(pair[0], pair[1]);
							}
						}
					}
					result.add(new Relation(source, target, targetLabel, properties));
				}
				return result;
			}
			return null;
		}

		private Map<String, String> extractMetaData(JsonOntologyTerm jsonTerm) {
			return JsonTermMetaData.getMap(jsonTerm.getMetaData());
		}

		private List<String> extractDefXRef(JsonOntologyTerm jsonTerm) {
			String[] refs = jsonTerm.getDefxRef();
			if (refs.length > 0) {
				return Arrays.asList(refs);
			}
			return null;
		}

		private List<Synonym> extractSynonyms(JsonOntologyTerm jsonTerm) {
			JsonSynonym[] synonyms = jsonTerm.getSynonyms();
			if (synonyms != null && synonyms.length > 0) {
				List<Synonym> result = new ArrayList<Synonym>(synonyms.length);
				for (JsonSynonym synonym : synonyms) {
					String label = synonym.getLabel();
					String scope = synonym.getScope();
					String category = synonym.getCategory();
					Set<String> xrefs = null;
					String[] jsonXRefs = synonym.getXrefs();
					if (jsonXRefs != null && jsonXRefs.length > 0) {
						xrefs = new HashSet<String>(Arrays.asList(jsonXRefs));
					}
					result.add(new Synonym(label, scope, category, xrefs));
				}
				return result;
			}
			return null;
		}

	}

	/**
	 * Create {@link CommitInfo} instance for the given modifications.
	 * 
	 * @param terms
	 * @param relations
	 * @param termgenieUser
	 * @return CommitInfo
	 */
	protected abstract CommitInfo createCommitInfo(List<CommitObject<OntologyTerm>> terms,
			List<CommitObject<Relation>> relations,
			String termgenieUser);

	// ---------------- Helper ----------------

	/**
	 * Helper to handle the mapping of temporary identifiers to ontology
	 * specific new identifiers. Also used to check that all identifiers used in
	 * Relations are mapped.
	 */
	private static class IdHandler {

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
			this.tempIdPrefix = tempIdPrefix.toLowerCase();
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
			Set<String> missingIds = new HashSet<String>(used);
			missingIds.removeAll(tempIdMap.keySet());
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
		return error("The commit operation did not succeed, exception: " + exception.getMessage());
	}
}
