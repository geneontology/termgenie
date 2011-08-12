package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CommitAwareOntologyLoader extends ReloadingOntologyLoader {

	private final EntityManager entityManager;

	@Inject
	CommitAwareOntologyLoader(OntologyConfiguration configuration,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit,
			EntityManager entityManager)
	{
		super(configuration, iriMapper, cleaner, skipOntologies, period, unit);
		this.entityManager = entityManager;

	}

	@Override
	protected OWLGraphWrapper load(Ontology ontology, String url)
			throws OWLOntologyCreationException, IOException
	{
		OWLGraphWrapper wrapper = super.load(ontology, url);
		if (wrapper != null) {
			addCommitedTerms(ontology, wrapper);
		}
		return wrapper;
	}

	private void addCommitedTerms(Ontology ontology, OWLGraphWrapper wrapper) {
		TypedQuery<CommitedOntologyTerm> query = entityManager.createQuery("select t from CommitedOntologyTerm as t where t.ontology = ?1",
				CommitedOntologyTerm.class);
		query.setParameter(1, ontology.getUniqueName());
		List<CommitedOntologyTerm> terms = query.getResultList();
		for (CommitedOntologyTerm term : terms) {
			addTerm(term, wrapper);
		}
	}

	private void addTerm(CommitedOntologyTerm term, OWLGraphWrapper wrapper) {
		// create owl description

		// check if the term is already in the ontology via id

		// check if a term with the same label exists

		// add term to owl
		throw new RuntimeException("Not implemented");
	}

}
