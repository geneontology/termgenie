package org.bbop.termgenie.services.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;


public class RecentSubmissionsServiceImpl implements RecentSubmissionsService {
	
	private static final Logger logger = Logger.getLogger(RecentSubmissionsServiceImpl.class);

	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final OntologyTaskManager source;
	
	@Inject
	public RecentSubmissionsServiceImpl(InternalSessionHandler sessionHandler,
			OntologyLoader loader,
			UserPermissions permissions)
	{
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.source = loader.getOntologyManager();
	}

	private CommitHistoryStore historyStore = null;
	
	
	/**
	 * @param historyStore the historyStore to set
	 */
	@Inject(optional=true)
	@Nullable
	public void setHistoryStore(CommitHistoryStore historyStore) {
		this.historyStore = historyStore;
	}
	
	@Override
	public boolean isEnabled() {
		return historyStore != null && source != null;
	}
	
	@Override
	public boolean canView(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowCommitReview = permissions.allowCommit(userData, source.getOntology());
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonRecentSubmission[] getRecentTerms(String sessionId, HttpSession session) {
		JsonRecentSubmission[] recent = null;
		if (historyStore != null && source != null) {
			if (canView(sessionId, session)) {
				recent = getRecentTermsInternal(recent);
			}
		}
		return recent;
	}

	private JsonRecentSubmission[] getRecentTermsInternal(JsonRecentSubmission[] recent) {
		String ontologyName = source.getOntology().getName();
		try {
			Calendar cal = new GregorianCalendar();
			cal.setTime(new Date());
			cal.roll(Calendar.YEAR, false);
			Date oneYearBefore = cal.getTime();
			List<CommitHistoryItem> items = historyStore.load(ontologyName, oneYearBefore, null);
			if (items != null) {
				List<JsonRecentSubmission> recentSubmissions = new ArrayList<JsonRecentSubmission>(items.size());
				for (CommitHistoryItem item : items) {
					boolean committed = item.isCommitted();
					String savedBy = item.getSavedBy();
					Date dateObj = item.getDate();
					String dateString = null;
					if (dateObj != null) {
						dateString = formatDate(dateObj);
					}

					List<CommitedOntologyTerm> terms = item.getTerms();
					for (CommitedOntologyTerm term : terms) {
						JsonRecentSubmission json = new JsonRecentSubmission();
						json.content = term.getObo();
						json.lbl = term.getLabel();
						json.pattern = term.getPattern();
						json.committed = committed;
						json.user = savedBy;
						json.date = dateString;
						if (committed) {
							json.msg = item.getCommitMessage();
						}
						recentSubmissions.add(json);
					}
				}
				recent = recentSubmissions.toArray(new JsonRecentSubmission[recentSubmissions.size()]);
			}
		} catch (CommitHistoryStoreException exception) {
			logger.error("Could not load histoy for ontology: "+ontologyName, exception);
		}
		return recent;
	}

	private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
		
	};
	
	private String formatDate(Date date) {
		return dateFormat.get().format(date);
	}
}
