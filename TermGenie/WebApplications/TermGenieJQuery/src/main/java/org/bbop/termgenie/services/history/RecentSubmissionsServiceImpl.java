package org.bbop.termgenie.services.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class RecentSubmissionsServiceImpl implements RecentSubmissionsService {
	
	private static final Logger logger = Logger.getLogger(RecentSubmissionsServiceImpl.class);

	private CommitHistoryStore historyStore = null;
	private OntologyTaskManager source = null;
	
	/**
	 * @param historyStore the historyStore to set
	 */
	@Inject(optional=true)
	@Nullable
	public void setHistoryStore(CommitHistoryStore historyStore) {
		this.historyStore = historyStore;
	}
	
	@Inject(optional=true)
	@Nullable
	public void setSource(@Named("CommitTargetOntology") OntologyTaskManager source) {
		this.source = source;
	}

	@Override
	public boolean isEnabled() {
		return historyStore != null && source != null;
	}

	@Override
	public JsonRecentSubmission[] getRecentTerms(String sessionId, HttpSession session) {
		JsonRecentSubmission[] recent = null;
		if (historyStore != null && source != null) {
			String ontologyName = source.getOntology().getUniqueName();
			try {
				CommitHistory history = historyStore.loadHistory(ontologyName);
				if (history != null) {
					List<CommitHistoryItem> items = history.getItems();
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
				}
				
			} catch (CommitHistoryStoreException exception) {
				logger.error("Could not load histoy for ontology: "+ontologyName, exception);
			}
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
