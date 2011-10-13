package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;

import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;

import com.google.inject.Inject;

public class CommitHistoryStoreImpl implements CommitHistoryStore {

	private EntityManager entityManager;

	/**
	 * @param entityManager
	 */
	@Inject
	CommitHistoryStoreImpl(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}

	@Override
	public void add(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		try {
			CommitHistory history = loadHistory(ontology);
			if (history == null) {
				history = new CommitHistory();
				history.setOntology(ontology);
				ArrayList<CommitHistoryItem> items = new ArrayList<CommitHistoryItem>();
				items.add(item);
				history.setItems(items);
				entityManager.persist(history);
			}
			else {
				synchronized (history) {
					history.getItems().add(item);
					entityManager.merge(history);
				}
			}
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (EntityExistsException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		}
	}

	@Override
	public void update(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		try {
			entityManager.merge(item);
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		}
	}

	@Override
	public void remove(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		try {
			entityManager.remove(item);
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		}
	}

	private CommitHistory loadHistory(String ontology) throws CommitHistoryStoreException {
		try {
			String qlString = "SELECT history FROM CommitHistory history WHERE history.ontology = ?1";
			TypedQuery<CommitHistory> query = entityManager.createQuery(qlString, CommitHistory.class);
			query.setParameter(1, ontology);
			List<CommitHistory> results = query.getResultList();
			if (results != null && !results.isEmpty()) {
				CommitHistory history = results.get(0);
				return history;
			}
			return null;
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (QueryTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (PessimisticLockException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (LockTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		} catch (PersistenceException exception) {
			throw new CommitHistoryStoreException("Could not execute db read", exception);
		}
	}

	@Override
	public List<CommitHistoryItem> load(String ontology, Date from, Date to)
			throws CommitHistoryStoreException
	{
		boolean hasFrom = from != null;
		boolean hasTo = to != null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT history.items FROM CommitHistory history, IN(history.items) items WHERE history.ontology = ?1");
		if (hasFrom) {
			sb.append(" AND items.date >= ?2");
		}
		if (hasTo) {
			sb.append(" AND items.date <= ?");
			int param = hasFrom ? 3 : 2;
			sb.append(param);
		}

		try {
			TypedQuery<CommitHistoryItem> query = entityManager.createQuery(sb.toString(),
					CommitHistoryItem.class);
			query.setParameter(1, ontology);
			if (hasFrom) {
				query.setParameter(2, from);
			}
			if (hasTo) {
				int param = hasFrom ? 3 : 2;
				query.setParameter(param, to);
			}
			List<CommitHistoryItem> resultList = query.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				return new ArrayList<CommitHistoryItem>(resultList);
			}
			return null;
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		}
	}

	@Override
	public List<CommitHistoryItem> load(List<Integer> itemIds) throws CommitHistoryStoreException {
		String queryString = "SELECT history.items FROM CommitHistory history, IN(history.items) items WHERE items.id IN (?1)";
		try {
			TypedQuery<CommitHistoryItem> query = entityManager.createQuery(queryString, CommitHistoryItem.class);
			query.setParameter(1, itemIds);
			List<CommitHistoryItem> resultList = query.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				return new ArrayList<CommitHistoryItem>(resultList);
			}
			return null;
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		}
	}
}
