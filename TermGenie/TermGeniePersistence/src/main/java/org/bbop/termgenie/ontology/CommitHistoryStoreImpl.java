package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Collections;
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

import org.apache.log4j.Logger;
import org.apache.openjpa.persistence.InvalidStateException;
import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.tools.Pair;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CommitHistoryStoreImpl implements CommitHistoryStore {

	private static final Logger logger = Logger.getLogger(CommitHistoryStoreImpl.class);
	
	private EntityManagerFactory entityManagerFactory;

	/**
	 * @param entityManagerFactory
	 */
	@Inject
	CommitHistoryStoreImpl(@Named("DefaultEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		super();
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void add(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		Throwable e = null;
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
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (IllegalArgumentException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (EntityExistsException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update", exception);
		} catch (CommitHistoryStoreException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw exception;
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public void update(CommitHistoryItem item, String ontology) throws CommitHistoryStoreException {
		Throwable e = null;
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(item);
			entityManager.flush();
			entityManager.getTransaction().commit();
		} catch (IllegalStateException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (IllegalArgumentException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public void remove(final CommitHistoryItem item, final String ontology) throws CommitHistoryStoreException {
		Throwable e = null;
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
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw new CommitHistoryStoreException("Could not execute db update (delete)", exception);
		} catch (CommitHistoryStoreException exception) {
			e = exception;
			entityManager.getTransaction().rollback();
			throw exception;
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public CommitHistory loadHistory(String ontology) throws CommitHistoryStoreException {
		Throwable e = null;
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
		} catch (CommitHistoryStoreException exception) {
			e = exception;
			throw exception;
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}
	
	@Override
	public List<CommitHistoryItem> loadHistory(String ontology, boolean committed)
			throws CommitHistoryStoreException
	{
		return getItems(ontology, committed);
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
		Throwable e = null;
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
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public List<CommitHistoryItem> load(List<Integer> itemIds) throws CommitHistoryStoreException {
		Throwable e = null;
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
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public List<CommitHistoryItem> getItemsForReview(String ontology)
			throws CommitHistoryStoreException
	{
		return getItems(ontology, false);
	}
	
	List<CommitHistoryItem> getItems(String ontology, boolean committed)
			throws CommitHistoryStoreException
	{
		Throwable e = null;
		String queryString = "SELECT items FROM CommitHistory history, IN(history.items) items WHERE (history.ontology = ?1) AND (items.committed=?2)";
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<CommitHistoryItem> query = entityManager.createQuery(queryString, CommitHistoryItem.class);
			query.setParameter(1, ontology);
			query.setParameter(2, committed);
			List<CommitHistoryItem> resultList = query.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				return new ArrayList<CommitHistoryItem>(resultList);
			}
			return null;
		} catch (IllegalArgumentException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}

	@Override
	public List<Pair<String, String>> checkRecentCommits(String ontology, List<String> labels) throws CommitHistoryStoreException {
		Throwable e = null;
		String queryString = "SELECT terms FROM CommitHistory history, IN(history.items) items, IN(items.terms) terms WHERE (history.ontology = ?1) AND (items.committed=false) AND (terms.label IN ?2)";
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<CommitedOntologyTerm> query = entityManager.createQuery(queryString, CommitedOntologyTerm.class);
			query.setParameter(1, ontology);
			query.setParameter(2, labels);
			List<CommitedOntologyTerm> resultList = query.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				List<Pair<String, String>> pairs = new ArrayList<Pair<String,String>>();
				for (CommitedOntologyTerm term : resultList) {
					pairs.add(new Pair<String, String>(term.getId(), term.getLabel()));
				}
				return pairs;
			}
			return Collections.emptyList();
		} catch (IllegalArgumentException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (IllegalStateException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (QueryTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (TransactionRequiredException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PessimisticLockException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (LockTimeoutException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} catch (PersistenceException exception) {
			e = exception;
			throw new CommitHistoryStoreException("Could not execute db load", exception);
		} finally {
			try {
				entityManager.close();
			} catch (InvalidStateException exception) {
				if (e == null) {
					throw new CommitHistoryStoreException("Could not execute db update (merge)", exception);
				}
				logger.error("Could not close manager, after exception "+e.getMessage(), exception);
			}
		}
	}
}
