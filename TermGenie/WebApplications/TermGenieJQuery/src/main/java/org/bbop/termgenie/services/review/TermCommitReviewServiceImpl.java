package org.bbop.termgenie.services.review;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.LoadState;
import org.bbop.termgenie.ontology.obo.OBOWriterTools;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class TermCommitReviewServiceImpl implements TermCommitReviewService {

	private static final Logger logger = Logger.getLogger(TermCommitReviewServiceImpl.class);
	
	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final OntologyCommitReviewPipelineStages stages;
	private final OntologyTaskManager ontology;
	
	@Inject
	TermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("TermCommitReviewServiceOntology") OntologyTaskManager ontology,
			OntologyCommitReviewPipelineStages stages)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.ontology = ontology;
		this.stages = stages;
	}

	@Override
	public boolean isEnabled() {
		Committer reviewCommitter = stages.getReviewCommitter();
		return reviewCommitter != null;
	}

	@Override
	public boolean isAuthorized(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			String guid = sessionHandler.getGUID(session);
			if (guid != null) {
				boolean allowCommitReview = permissions.allowCommitReview(guid, ontology.getOntology());
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session) {
		if (isAuthorized(sessionId, session)) {
			BeforeReview beforeReview = stages.getBeforeReview();
			try {
				List<CommitHistoryItem> items = beforeReview.getItemsForReview();
				if (items != null && !items.isEmpty()) {
					List<JsonCommitReviewEntry> result = createEntries(items);
					
					if (!result.isEmpty()) {
						return result.toArray(new JsonCommitReviewEntry[result.size()]);
					}
				}
			} catch (CommitException exception) {
				logger.error("Could not retrieve pending commits for db", exception);
			}
		}
		return null;
	}

	protected List<JsonCommitReviewEntry> createEntries(List<CommitHistoryItem> items) {
		CreateOboDocTask task = new CreateOboDocTask();
		ontology.runManagedTask(task);
		List<JsonCommitReviewEntry> result = new ArrayList<JsonCommitReviewEntry>(items.size());
		for (CommitHistoryItem item : items) {
			JsonCommitReviewEntry entry = new JsonCommitReviewEntry();
			entry.setHistoryId(item.getId());
			entry.setDate(formatDate(item.getDate()));
			entry.setUser(item.getUser());
			entry.setDiffs(createJsonDiffs(item, task.result));
			result.add(entry);
		}
		return result;
	}
	
	private static class CreateOboDocTask extends OntologyTask {

		OBODoc result = null;

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			Owl2Obo owl2Obo = new Owl2Obo();
			result = owl2Obo.convert(managed.getSourceOntology());
		}
	}
	
	private JsonDiff[] createJsonDiffs(CommitHistoryItem item, OBODoc oboDoc) {
		List<JsonDiff> result = new ArrayList<JsonDiff>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			JsonDiff jsonDiff = new JsonDiff();
			jsonDiff.setOperation(term.getOperation());
			
			Modification mode = CommitHistoryTools.getModification(term.getOperation());
			LoadState state = ComitAwareOBOConverterTools.handleTerm(term, mode, oboDoc);
			if (LoadState.isSuccess(state)) {
				try {
					jsonDiff.setDiff(OBOWriterTools.writeTerm(term.getId(), oboDoc));
					result.add(jsonDiff);
				} catch (IOException exception) {
					logger.error("Could not create diff for pending commit", exception);
				}
			}
		}
		if (!result.isEmpty()) {
			return result.toArray(new JsonDiff[result.size()]);
		}
		return null;
	}
	
	private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat();
		}
		
	};
	
	private String formatDate(Date date) {
		return dateFormat.get().format(date);
	}

	@Override
	public JsonResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session)
	{
		if (!isAuthorized(sessionId, session)) {
			return new JsonResult(false, "Error: This commit is not authorized.");
		}
		if (entries == null || entries.length == 0) {
			return new JsonResult(false, "Error: No entires to commit in the request.");
		}
		
		List<Integer> historyIds = new ArrayList<Integer>(entries.length);
		for (JsonCommitReviewEntry entry : entries) {
			historyIds.add(entry.getHistoryId());
		}
			
		// commit changes to repository
		AfterReview afterReview = stages.getAfterReview();
			
		try {
			afterReview.commit(historyIds);
			return new JsonResult(true);
		} catch (CommitException exception) {
			logger.error("Error during commit", exception);
			return new JsonResult(false, "Error during commit: "+exception.getMessage());
		}
	}

}
