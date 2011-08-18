package org.bbop.termgenie.ontology;

import java.util.Date;
import java.util.List;

import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;

/**
 * Define methods relevant for storing and retrieving commits.
 */
public interface CommitHistoryStore {

	/**
	 * A commit store specific exception.
	 */
	public static class CommitHistoryStoreException extends Exception {

		// generated
		private static final long serialVersionUID = -9191882251819480184L;

		public CommitHistoryStoreException(String message) {
			super(message);
		}
		
		public CommitHistoryStoreException(String message, Throwable exception) {
			super(message, exception);
		}
	}
	
	/**
	 * Store or update a {@link CommitHistory}.
	 * 
	 * @param history
	 * @throws CommitHistoryStoreException
	 */
	public void store(CommitHistory history) throws CommitHistoryStoreException;
	
	/**
	 * Retrieve a {@link CommitHistory} for a given ontology.
	 * 
	 * @param ontology the unique name of the ontology
	 * @return the corresponding {@link CommitException} or null
	 * @throws CommitHistoryStoreException
	 */
	public CommitHistory loadAll(String ontology) throws CommitHistoryStoreException;
	
	/**
	 * Retrieve the {@link CommitHistoryItem} for a given ontology and date range
	 * 
	 * @param ontology the unique name of the ontology 
	 * @param from date or null
	 * @param to date or null
	 * @return list of {@link CommitHistoryItem}
	 * @throws CommitHistoryStoreException
	 */
	public List<CommitHistoryItem> load(String ontology, Date from, Date to) throws CommitHistoryStoreException;
}
