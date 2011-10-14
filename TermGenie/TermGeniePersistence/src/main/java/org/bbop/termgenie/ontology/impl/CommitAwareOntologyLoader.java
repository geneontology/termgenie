package org.bbop.termgenie.ontology.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CommitAwareOntologyLoader extends ReloadingOntologyLoader {

	private final static Logger logger = Logger.getLogger(CommitAwareOntologyLoader.class);
	
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
	protected void postProcessOWLOntology(String ontology, OWLOntology owlOntology) {
		// TODO Auto-generated method stub
		super.postProcessOWLOntology(ontology, owlOntology);
	}

	@Override
	protected void postProcessOBOOntology(String ontology, OBODoc obodoc) {
		super.postProcessOBOOntology(ontology, obodoc);
		
		// apply history to ontology
		// try to detect, whether the changes have been 
		// committed to the loaded ontology version, or if conflicts exist.
		TypedQuery<CommitHistory> query = entityManager.createQuery("select h from CommitHistory as h where h.ontology = ?1",
				CommitHistory.class);
		query.setParameter(1, ontology);
		List<CommitHistory> histories = query.getResultList();
		if (!histories.isEmpty()) {
			if (histories.size() != 1) {
				logger.error("Multiple histories ("+histories.size()+") found for ontology: "+ontology);
			}
			CommitHistory history = histories.get(0);
			List<CommitHistoryItem> items = history.getItems();
			for (CommitHistoryItem item : items) {
				if (item.isCommitted()) {
					List<CommitedOntologyTerm> terms = item.getTerms();
					if (terms != null) {
						for (CommitedOntologyTerm term : terms) {
							int operation = term.getOperation();
							if (operation >= 0) {
								Modification mode = Modification.values()[operation];
								ComitAwareOBOConverterTools.handleTerm(term, mode, obodoc);
							}
						}
					}
				}
			}
		}
	}

}
