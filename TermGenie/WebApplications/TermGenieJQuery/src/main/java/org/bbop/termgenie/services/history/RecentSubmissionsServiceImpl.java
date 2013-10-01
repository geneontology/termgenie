package org.bbop.termgenie.services.history;

import javax.servlet.http.HttpSession;


public class RecentSubmissionsServiceImpl implements RecentSubmissionsService {

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public JsonRecentSubmission[] getRecentTerms(String sessionId, HttpSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
