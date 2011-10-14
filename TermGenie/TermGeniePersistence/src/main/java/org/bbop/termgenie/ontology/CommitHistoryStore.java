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
	 * Add an {@link CommitHistoryItem} to the {@link CommitHistory}.
	 * 
	 * @param item
	 * @param ontology
	 * @throws CommitHistoryStoreException
	 */
	public void add(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException;

	/**
	 * Update an {@link CommitHistoryItem} to the {@link CommitHistory}.
	 * 
	 * @param item
	 * @param ontology
	 * @throws CommitHistoryStoreException
	 */
	public void update(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException;

	/**
	 * Remove an {@link CommitHistoryItem} to the {@link CommitHistory}.
	 * 
	 * @param item
	 * @param ontology
	 * @throws CommitHistoryStoreException
	 */
	public void remove(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException;

	/**
	 * Retrieve the {@link CommitHistoryItem} for a given ontology and date
	 * range
	 * 
	 * @param ontology the unique name of the ontology
	 * @param from date or null
	 * @param to date or null
	 * @return list of {@link CommitHistoryItem}
	 * @throws CommitHistoryStoreException
	 */
	public List<CommitHistoryItem> load(String ontology, Date from, Date to)
			throws CommitHistoryStoreException;

	/**
	 * Retrieve the {@link CommitHistoryItem} for a given set of identifiers
	 * 
	 * @param itemIds identifiers of the items to retrieve
	 * @return list of {@link CommitHistoryItem}
	 * @throws CommitHistoryStoreException
	 */
	public List<CommitHistoryItem> load(List<Integer> itemIds) throws CommitHistoryStoreException;

	/**
	 * Retrieve the {@link CommitHistoryItem} for a which need to be reviewed.
	 * 
	 * @param ontology
	 * @return list of {@link CommitHistoryItem}
	 * @throws CommitHistoryStoreException
	 */
	public List<CommitHistoryItem> getItemsForReview(String ontology)
			throws CommitHistoryStoreException;

}
