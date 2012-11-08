package org.bbop.termgenie.services.review;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools;
import org.bbop.termgenie.ontology.obo.OboParserTools;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter.OBODocNameProvider;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
	private final Ontology ontology;
	private final MultiOntologyTaskManager manager;
	
	@Inject
	TermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("CommitTargetOntology") OntologyTaskManager ontology,
			MultiOntologyTaskManager manager,
			OntologyCommitReviewPipelineStages stages)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.ontology = ontology.getOntology();
		this.manager = manager;
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
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowCommitReview = permissions.allowCommitReview(userData, ontology);
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
						// update commit message to include the user doing the review
						UserData userData = sessionHandler.getUserData(session);
						if (userData == null) {
							logger.warn("Could not retrieve user data for session: "+sessionId);
							return null;
						}
						String scmAlias = userData.getScmAlias();
						for (JsonCommitReviewEntry entry : result) {
							StringBuilder sb = new StringBuilder(entry.getCommitMessage());
							sb.append(" reviewed by ").append(scmAlias);
							entry.setCommitMessage(sb.toString());
							entry.setCommitMessageChanged(true);
						}
						return result.toArray(new JsonCommitReviewEntry[result.size()]);
					}
				}
			} catch (CommitException exception) {
				logger.error("Could not retrieve pending commits for db", exception);
			} catch (OWLOntologyCreationException exception) {
				logger.error("Could not create entries for items", exception);
			} catch (InvalidManagedInstanceException exception) {
				logger.error("Could not create entries for items due to invalid ontology", exception);
			}
		}
		return null;
	}

	protected List<JsonCommitReviewEntry> createEntries(List<CommitHistoryItem> items) throws OWLOntologyCreationException, InvalidManagedInstanceException {
		CreateOboDocTask task = new CreateOboDocTask();
		manager.runManagedTask(task, ontology);
		List<JsonCommitReviewEntry> result = new ArrayList<JsonCommitReviewEntry>(items.size());
		for (CommitHistoryItem item : items) {
			JsonCommitReviewEntry entry = new JsonCommitReviewEntry();
			entry.setHistoryId(item.getId());
			entry.setVersion(item.getVersion());
			entry.setDate(formatDate(item.getDate()));
			entry.setCommitMessage(item.getCommitMessage());
			entry.setEmail(item.getEmail());
			if (task.exception != null) {
				throw task.exception;
			}
			entry.setDiffs(createJsonDiffs(item, task.result));
			result.add(entry);
		}
		return result;
	}
	
	private static class CreateOboDocTask extends MultiOntologyTask {

		OBODoc result = null;
		OWLOntologyCreationException exception;

		@Override
		public List<Modified> run(List<OWLGraphWrapper> requested) {
			Owl2Obo owl2Obo = new Owl2Obo();
			OWLGraphWrapper graph = requested.get(0);
			try {
				result = owl2Obo.convert(graph.getSourceOntology());
			} catch (OWLOntologyCreationException exception) {
				this.exception = exception;
			}
			return null;
		}
	}
	
	private List<JsonDiff> createJsonDiffs(CommitHistoryItem item, OBODoc oboDoc) {
		List<JsonDiff> result = new ArrayList<JsonDiff>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
			JsonDiff jsonDiff = new JsonDiff();
			jsonDiff.setOperation(term.getOperation());
			jsonDiff.setId(term.getId());
			jsonDiff.setUuid(term.getUuid());
			jsonDiff.setObsolete(OboTools.isObsolete(frame));
			jsonDiff.setPattern(term.getPattern());
			
			List<Pair<Frame, Set<OWLAxiom>>> changed = CommitHistoryTools.translateSimple(term.getChanged());
			boolean success = ComitAwareOboTools.handleTerm(frame, changed, term.getOperation(), oboDoc);
			if (success) {
				try {
					jsonDiff.setDiff(OboWriterTools.writeTerm(term.getId(), oboDoc));
					jsonDiff.setOwlAxioms(term.getAxioms());
					result.add(jsonDiff);
				} catch (IOException exception) {
					logger.error("Could not create diff for pending commit", exception);
				}
			}
			if (changed != null && !changed.isEmpty()) {
				jsonDiff.setRelations(JsonOntologyTerm.createJson(changed, new OBODocNameProvider(oboDoc)));
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
			HttpSession session,
			ProcessState state)
	{
		if (!isAuthorized(sessionId, session)) {
			return JsonCommitReviewCommitResult.error("Error: This commit is not authorized.");
		}
		if (entries == null || entries.length == 0) {
			return JsonCommitReviewCommitResult.error("Error: No entires to commit in the request.");
		}
		
		// commit changes to repository
		GenericTaskManager<AfterReview> afterReviewTaskManager = stages.getAfterReview();
		
		CommitTask task = new CommitTask(entries, this, state);
		try {
			afterReviewTaskManager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Error during commit", exception);
			return JsonCommitReviewCommitResult.error("Error during commit: "+exception.getMessage());
		}
		if (task.exception != null) {
			logger.error("Error during commit", task.exception);
			return JsonCommitReviewCommitResult.error("Error during commit: "+task.exception.getMessage());
		}
		JsonCommitReviewCommitResult json;
		try {
			json = JsonCommitReviewCommitResult.success(task.historyIds, task.commits, ontology, manager);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Could not create data for successfull commit result", exception);
			return JsonCommitReviewCommitResult.error("Commit successfull, but could not create data for successfull commit result display: "+exception.getMessage());
		}
		return json;
	}

	private static final class CommitTask implements ManagedTask<AfterReview> {
	
		private final JsonCommitReviewEntry[] entries;
		private List<Integer> historyIds;
		private List<CommitResult> commits;
		private CommitException exception;
		private final TermCommitReviewServiceImpl instance;
		private final ProcessState state;
	
		private CommitTask(JsonCommitReviewEntry[] entries,
				TermCommitReviewServiceImpl instance,
				ProcessState state)
		{
			this.entries = entries;
			this.instance = instance;
			this.state = state;
		}
	
		@Override
		public Modified run(AfterReview afterReview)
		{
			try {
				ProcessState.addMessage(state, "Start updating internal database");
				historyIds = new ArrayList<Integer>(entries.length);
				for (JsonCommitReviewEntry entry : entries) {
					if (entry.isCommitMessageChanged()) {
						updateHistoryItem(entry, afterReview);
					}
					else {
						List<JsonDiff> jsonDiffs = entry.getDiffs();
						for (JsonDiff jsonDiff : jsonDiffs) {
							if (jsonDiff.isModified()) {
								updateHistoryItem(entry, afterReview);
								break;
							}
						}
					}
					historyIds.add(entry.getHistoryId());
				}
				ProcessState.addMessage(state, "Finished updating internal database");
				
				commits = afterReview.commit(historyIds, state);
				
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
			if (entry.isCommitMessageChanged()) {
				historyItem.setCommitMessage(entry.getCommitMessage());
			}
			StringBuilder sb = new StringBuilder(historyItem.getCommitMessage());
			List<JsonDiff> diffs = entry.getDiffs();
			for (JsonDiff jsonDiff : diffs) {
				Frame termFrame = parseDiff(jsonDiff);
				CommitedOntologyTerm term = getMatchingTerm(historyItem, jsonDiff.getUuid());
				if (jsonDiff.isModified()) {
					CommitHistoryTools.update(term, termFrame, jsonDiff.getOwlAxioms(), JsonDiff.getModification(jsonDiff));
				}
				sb.append('\n');
				if(OboTools.isObsolete(termFrame)) {
					term.setChanged(null); // do not change any relations for other terms
					sb.append("OBSOLETE ");
				}
				else {
					sb.append("Added ");
				}
				sb.append(termFrame.getId());
				Object label = termFrame.getTagValue(OboFormatTag.TAG_NAME);
				if (label != null) {
					sb.append(" ");
					sb.append(label);
				}
			}
			historyItem.setCommitMessage(sb.toString());
			afterReview.updateItem(historyItem);
		}

		private Frame parseDiff(JsonDiff jsonDiff) {
			Frame frame = OboParserTools.parseFrame(jsonDiff.getId(), jsonDiff.getDiff());
			Collection<Clause> clauses = frame.getClauses();
			Iterator<Clause> iterator = clauses.iterator();
			String tagName = OboFormatTag.TAG_IS_OBSELETE.getTag();
			while (iterator.hasNext()) {
				Clause clause = iterator.next();
				if (tagName.equals(clause.getTag())) {
					iterator.remove();
				}
			}
			if (jsonDiff.isObsolete()) {
				Clause obsoleteClause = new Clause(OboFormatTag.TAG_IS_OBSELETE);
				obsoleteClause.addValue(Boolean.TRUE);
				frame.addClause(obsoleteClause);
				
				instance.handleObsoleteFrame(jsonDiff, frame);
			}
			return frame;
		}

		private CommitedOntologyTerm getMatchingTerm(CommitHistoryItem historyItem,
				int uuid) throws CommitException
		{
			List<CommitedOntologyTerm> terms = historyItem.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				CommitedOntologyTerm original = terms.get(i);
				if (uuid == original.getUuid()) {
					return terms.get(i);
				}
			}
			throw new CommitException("Could not find the modified term in the history", false);
		}
	}

	/**
	 * Override this method to change the implement additional modifications for obsolete frames.
	 * 
	 * @param jsonDiff
	 * @param frame
	 */
	protected void handleObsoleteFrame(JsonDiff jsonDiff, Frame frame) {
		// remove all relations
		OboTools.removeAllRelations(frame);
		
		// remove all references to this term
	}
	
}
