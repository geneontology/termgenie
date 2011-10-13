package org.bbop.termgenie.services.review;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class TermCommitReviewServiceImpl implements TermCommitReviewService {

	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final OntologyCommitReviewPipelineStages stages;
	private final Ontology ontology;
	
	@Inject
	TermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			@Named("TermCommitReviewServiceOntology") Ontology ontology,
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
				boolean allowCommitReview = permissions.allowCommitReview(guid, ontology);
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session) {
		if (isAuthorized(sessionId, session)) {
			BeforeReview beforeReview = stages.getBeforeReview();
//			List<CommitHistoryItem> items = beforeReview.getItemsForReview();
		}
		return null;
	}

	@Override
	public JsonCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session)
	{
		if (isAuthorized(sessionId, session)) {
			// apply changes by the reviewer and store them in the history
			// TODO
			
			// write changes to repository
			AfterReview afterReview = stages.getAfterReview();
//			afterReview.commit(historyIds, mode, username, password);
		}
		return null;
	}

}
