package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
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
	public void store(CommitHistory history) throws CommitHistoryStoreException {
		entityManager.persist(history);
	}

	@Override
	public CommitHistory loadAll(String ontology) throws CommitHistoryStoreException {
		String qlString = "SELECT history FROM CommitHistory history " + "WHERE history.ontology = ?1";
		TypedQuery<CommitHistory> query = entityManager.createQuery(qlString, CommitHistory.class);
		query.setParameter(1, ontology);
		List<CommitHistory> results = query.getResultList();
		if (results != null && !results.isEmpty()) {
			CommitHistory history = results.get(0);
			return history;
		}
		return null;
	}

	@Override
	public List<CommitHistoryItem> load(String ontology, Date from, Date to)
			throws CommitHistoryStoreException
	{
		boolean hasFrom = from != null;
		boolean hasTo = to != null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT history.items FROM CommitHistory history, WHERE history.ontology = ?1");
		if (hasFrom) {
			sb.append(" AND history.items.date >= ?2");
		}
		if (hasTo) {
			sb.append(" AND history.items.date <= ?");
			int param = hasFrom ? 3 : 2;
			sb.append(param);
		}

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
	}

}
