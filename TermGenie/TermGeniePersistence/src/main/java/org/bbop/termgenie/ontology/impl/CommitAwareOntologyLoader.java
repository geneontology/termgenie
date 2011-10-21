package org.bbop.termgenie.ontology.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
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
	
	private final CommitHistoryStore commitHistoryStore;
	
	@Inject
	CommitAwareOntologyLoader(OntologyConfiguration configuration,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit,
			CommitHistoryStore commitHistoryStore)
	{
		super(configuration, iriMapper, cleaner, skipOntologies, period, unit);
		this.commitHistoryStore = commitHistoryStore;
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
		try {
			CommitHistory history = commitHistoryStore.loadHistory(ontology);
			if (history == null) {
				// do nothing
				return;
			}
			List<CommitHistoryItem> items = history.getItems();
			for (CommitHistoryItem item : items) {
				if (item.isCommitted()) {
					List<CommitedOntologyTerm> terms = item.getTerms();
					if (terms != null) {
						for (CommitedOntologyTerm term : terms) {
							ComitAwareOBOConverterTools.handleTerm(term, term.getOperation(), obodoc);
						}
					}
				}
			}
		} catch (CommitHistoryStoreException exception) {
			logger.error("Could not apply commit history to ontology: "+ontology, exception);
			throw new RuntimeException(exception);
		}
	}

}
