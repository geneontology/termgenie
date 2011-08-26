package org.bbop.termgenie.ontology;

import javax.persistence.EntityManager;

import org.bbop.termgenie.core.Ontology;

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
	public String getNewId(Ontology ontology) {
		return store.getNewId(ontology, entityManager);
	}

}
