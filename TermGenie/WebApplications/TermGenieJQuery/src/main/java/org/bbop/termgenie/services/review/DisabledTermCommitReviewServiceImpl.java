package org.bbop.termgenie.services.review;

import javax.servlet.http.HttpSession;

/**
 * Disabled default implementation for {@link TermCommitReviewService}.
 */
public class DisabledTermCommitReviewServiceImpl implements TermCommitReviewService {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isAuthorized(String sessionId, HttpSession session) {
		return false;
	}

	@Override
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session) {
		return null;
	}

	@Override
	public JsonCommitReviewCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session)
	{
		return null;
	}

}
