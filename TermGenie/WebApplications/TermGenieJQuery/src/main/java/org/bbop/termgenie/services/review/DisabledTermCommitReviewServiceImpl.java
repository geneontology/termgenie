package org.bbop.termgenie.services.review;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;

/**
 * Disabled default implementation for {@link TermCommitReviewService}.
 */
public class DisabledTermCommitReviewServiceImpl implements TermCommitReviewService {

	private final static JsonReviewConfig CONFIG;
	static  {
		CONFIG = new JsonReviewConfig();
		CONFIG.isEnabled = false;
	}
	
	@Override
	public JsonReviewConfig getConfig() {
		return CONFIG;
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
			HttpSession session,
			ProcessState state)
	{
		return null;
	}

}
