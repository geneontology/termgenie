package org.bbop.termgenie.ontology;

import javax.persistence.EntityManager;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.tools.Pair;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of an {@link OntologyIdProvider} using the
 * {@link OntologyIdStore}.
 */
@Singleton
public class OntologyIdProviderImpl implements OntologyIdProvider {

	private final OntologyIdStore store;
	private final EntityManager entityManager;

	@Inject
	OntologyIdProviderImpl(OntologyIdStoreConfiguration configuration, EntityManager entityManager)
	{
		super();
		this.entityManager = entityManager;
		store = new OntologyIdStore(configuration, entityManager);
	}

	@Override
	public Pair<String, Integer> getNewId(Ontology ontology) {
		return store.getNewId(ontology, entityManager);
	}

	@Override
	public boolean rollbackId(Ontology ontology, Integer id) {
		return store.rollbackId(ontology, id, entityManager);
	}

}
