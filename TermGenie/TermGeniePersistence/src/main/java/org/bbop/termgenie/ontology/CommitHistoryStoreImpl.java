package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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

	private EntityManagerFactory entityManagerFactory;

	/**
	 * @param entityManagerFactory
	 */
	@Inject
	CommitHistoryStoreImpl(EntityManagerFactory entityManagerFactory) {
		super();
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void add(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			CommitHistory history = loadHistory(ontology, entityManager);
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
					List<CommitHistoryItem> items = history.getItems();
					items.add(item);
					entityManager.merge(history);
				}
			}
			entityManager.getTransaction().commit();
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (EntityExistsException exception) {
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void update(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(item);
			entityManager.flush();
			entityManager.getTransaction().commit();
		} catch (IllegalStateException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public void remove(final CommitHistoryItem item, final String ontology) throws CommitHistoryStoreException {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			CommitHistory history = loadHistory(ontology, entityManager);
			if (history != null) {
				List<CommitHistoryItem> items = history.getItems();
				Iterator<CommitHistoryItem> iterator = items.iterator();
				while (iterator.hasNext()) {
					CommitHistoryItem currentItem = iterator.next();
					if (currentItem.getId() == item.getId()) {
						iterator.remove();
						break;
					}
				}
			}
			entityManager.merge(history);
			entityManager.flush();
			entityManager.getTransaction().commit();
		} catch (IllegalArgumentException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		} catch (TransactionRequiredException exception) {
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public CommitHistory loadHistory(String ontology) throws CommitHistoryStoreException {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			CommitHistory find = loadHistory(ontology, entityManager);
			if (find != null) {
				// the items are loaded lazy
				// before detach, access the list to ensure that the items are loaded.
				find.getItems(); 
				entityManager.detach(find);
			}
			return find;
		} finally {
			entityManager.close();
		}
	}
	
	CommitHistory loadHistory(String ontology, EntityManager entityManager) throws CommitHistoryStoreException {
		try {
			CommitHistory find = entityManager.find(CommitHistory.class, ontology);
			return find;
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
		sb.append("SELECT items FROM CommitHistory history, IN(history.items) items WHERE history.ontology = ?1");
		if (hasFrom) {
			sb.append(" AND items.date >= ?2");
		}
		if (hasTo) {
			sb.append(" AND items.date <= ?");
			int param = hasFrom ? 3 : 2;
			sb.append(param);
		}
		EntityManager entityManager = entityManagerFactory.createEntityManager();
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
		} finally {
			entityManager.close();
		}
	}

	@Override
	public List<CommitHistoryItem> load(List<Integer> itemIds) throws CommitHistoryStoreException {
		String queryString = "SELECT items FROM CommitHistory history, IN(history.items) items WHERE items.id IN (?1)";
		EntityManager entityManager = entityManagerFactory.createEntityManager();
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
		} finally {
			entityManager.close();
		}
	}

	@Override
	public List<CommitHistoryItem> getItemsForReview(String ontology)
			throws CommitHistoryStoreException
	{
		String queryString = "SELECT items FROM CommitHistory history, IN(history.items) items WHERE (history.ontology = ?1) AND (items.committed=false)";
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<CommitHistoryItem> query = entityManager.createQuery(queryString, CommitHistoryItem.class);
			query.setParameter(1, ontology);
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
		} finally {
			entityManager.close();
		}
	}
}
