package org.bbop.termgenie.services.review;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TermCommitReviewServiceImpl implements TermCommitReviewService {

	private InternalSessionHandler sessionHandler;
	private UserPermissions permissions;
	
	/**
	 * @param sessionHandler
	 * @param permissions
	 */
	@Inject
	TermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isAuthorized(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			String guid = sessionHandler.getGUID(session);
			if (guid != null) {
				boolean allowCommitReview = permissions.allowCommitReview(guid);
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
