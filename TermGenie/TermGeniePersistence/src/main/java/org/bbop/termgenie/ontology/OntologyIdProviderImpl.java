package org.bbop.termgenie.ontology;

import javax.persistence.EntityManagerFactory;

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
	private final EntityManagerFactory entityManagerFactory;

	@Inject
	OntologyIdProviderImpl(OntologyIdStoreConfiguration configuration, EntityManagerFactory entityManagerFactory)
	{
		super();
		this.entityManagerFactory = entityManagerFactory;
		store = new OntologyIdStore(configuration, entityManagerFactory);
	}

	@Override
	public Pair<String, Integer> getNewId(Ontology ontology) {
		return store.getNewId(ontology, entityManagerFactory);
	}

	@Override
	public boolean rollbackId(Ontology ontology, Integer id) {
		return store.rollbackId(ontology, id, entityManagerFactory);
	}

}
