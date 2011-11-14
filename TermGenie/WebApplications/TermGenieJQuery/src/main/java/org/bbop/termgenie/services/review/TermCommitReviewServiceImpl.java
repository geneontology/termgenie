package org.bbop.termgenie.services.review;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.LoadState;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.ontology.obo.OBOWriterTools;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonRelationDiff;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;

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
			entry.setVersion(item.getVersion());
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
	
	private List<JsonDiff> createJsonDiffs(CommitHistoryItem item, OBODoc oboDoc) {
		List<JsonDiff> result = new ArrayList<JsonDiff>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			JsonDiff jsonDiff = new JsonDiff();
			jsonDiff.setOperation(term.getOperation());
			jsonDiff.setId(term.getId());
			jsonDiff.setUuid(term.getUuid());
			jsonDiff.setObsolete(term.isObsolete());
			
			LoadState state = ComitAwareOBOConverterTools.handleTerm(term, term.getChanged(), term.getOperation(), oboDoc);
			if (LoadState.isSuccess(state)) {
				try {
					jsonDiff.setDiff(OBOWriterTools.writeTerm(term.getId(), oboDoc));
					result.add(jsonDiff);
				} catch (IOException exception) {
					logger.error("Could not create diff for pending commit", exception);
				}
			}
			Map<String, List<IRelation>> groups = OBOConverterTools.groupChangedRelations(term.getChanged());
			if (groups != null) {
				List<JsonRelationDiff> modifiedRelations = new ArrayList<JsonRelationDiff>(groups.size());
				for(String termId : groups.keySet()) {
					JsonRelationDiff relationDiff = new JsonRelationDiff();
					relationDiff.setTermId(termId);
					try {
						relationDiff.setRelations(OBOWriterTools.writeRelations(termId, oboDoc));
						modifiedRelations.add(relationDiff);
					} catch (IOException exception) {
						logger.error("Could not create diff for pending commit", exception);
					}
				}
				if (!modifiedRelations.isEmpty()) {
					jsonDiff.setRelations(modifiedRelations);
				}
			}
		}
		if (!result.isEmpty()) {
			return result;
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
	public JsonCommitReviewCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session)
	{
		if (!isAuthorized(sessionId, session)) {
			return JsonCommitReviewCommitResult.error("Error: This commit is not authorized.");
		}
		if (entries == null || entries.length == 0) {
			return JsonCommitReviewCommitResult.error("Error: No entires to commit in the request.");
		}
		
		// commit changes to repository
		GenericTaskManager<AfterReview> afterReviewTaskManager = stages.getAfterReview();
		
		CommitTask task = new CommitTask(entries);
		afterReviewTaskManager.runManagedTask(task);
		if (task.exception != null) {
			logger.error("Error during commit", task.exception);
			return JsonCommitReviewCommitResult.error("Error during commit: "+task.exception.getMessage());
		}
		return JsonCommitReviewCommitResult.success(task.historyIds, task.commits);
	}

	private static final class CommitTask implements ManagedTask<AfterReview> {
	
		private final JsonCommitReviewEntry[] entries;
		private List<Integer> historyIds;
		private List<CommitResult> commits;
		private CommitException exception;
	
		private CommitTask(JsonCommitReviewEntry[] entries) {
			this.entries = entries;
		}
	
		@Override
		public Modified run(AfterReview afterReview)
		{
			try {
				historyIds = new ArrayList<Integer>(entries.length);
				for (JsonCommitReviewEntry entry : entries) {
					List<JsonDiff> jsonDiffs = entry.getDiffs();
					for (JsonDiff jsonDiff : jsonDiffs) {
						if (jsonDiff.isModified()) {
							updateHistoryItem(entry, afterReview);
							break;
						}
					}
					historyIds.add(entry.getHistoryId());
				}
				
				commits = afterReview.commit(historyIds);
				return Modified.no;
			} catch (CommitException exception) {
				this.exception = exception;
				return Modified.no;
			}
		}
		
		private void updateHistoryItem(JsonCommitReviewEntry entry, AfterReview afterReview) throws CommitException {
			int historyId = entry.getHistoryId();
			CommitHistoryItem historyItem = afterReview.getItem(historyId);
			if (entry.getVersion() != historyItem.getVersion()) {
				throw new CommitException("Could not change item, due to conflicting updates.", false);
			}
			if (historyItem.isCommitted()) {
				throw new CommitException("Trying to change an already committed item.", false);
			}
			List<JsonDiff> diffs = entry.getDiffs();
			for (JsonDiff jsonDiff : diffs) {
				if (jsonDiff.isModified()) {
					Frame termFrame = parseDiff(jsonDiff);
					CommitedOntologyTerm term = CommitHistoryTools.create(termFrame, JsonDiff.getModification(jsonDiff));
					updateMatchingTerm(historyItem, jsonDiff.getUuid(), term);
				}
			}
			afterReview.updateItem(historyItem);
		}

		private Frame parseDiff(JsonDiff jsonDiff) {
			boolean isObsolete = jsonDiff.isObsolete();
			OBOFormatParser p = new OBOFormatParser();
			p.setReader(new BufferedReader(new StringReader(jsonDiff.getDiff())));
			OBODoc obodoc = new OBODoc();
			p.parseTermFrame(obodoc);
			Frame termFrame = obodoc.getTermFrame(jsonDiff.getId());
			Clause clause = termFrame.getClause(OboFormatTag.TAG_IS_OBSELETE);
			if (clause == null) {
				clause = new Clause(OboFormatTag.TAG_IS_OBSELETE);
				termFrame.addClause(clause);
			}
			clause.setValue(Boolean.valueOf(isObsolete));
			return termFrame;
		}

		private void updateMatchingTerm(CommitHistoryItem historyItem,
				int uuid,
				CommitedOntologyTerm updatedTerm) throws CommitException
		{
			List<CommitedOntologyTerm> terms = historyItem.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				CommitedOntologyTerm original = terms.get(i);
				if (uuid == original.getUuid()) {
					updatedTerm.setChanged(original.getChanged());
					terms.set(i, updatedTerm);
					return;
				}
			}
			throw new CommitException("Could not find the modified term in the history", false);
		}
	}

}
