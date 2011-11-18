package org.bbop.termgenie.ontology.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools.LoadState;
import org.obolibrary.oboformat.model.Frame;
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
			List<CommitHistoryItem> items = commitHistoryStore.loadHistory(ontology, true);
			if (items != null) {
				for (CommitHistoryItem item : items) {
					List<CommitedOntologyTerm> terms = item.getTerms();
					if (terms != null) {
						List<CommitedOntologyTerm> redundant = new ArrayList<CommitedOntologyTerm>(terms.size());
						for (CommitedOntologyTerm term : terms) {
							Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
							List<Frame> changes = CommitHistoryTools.translateSimple(term.getChanged());
							LoadState state = ComitAwareOboTools.handleTerm(frame, changes, term.getOperation(), obodoc);
							if (!LoadState.isSuccess(state)) {
								if(LoadState.isError(state)) {
									logger.warn("Could not apply change item #"+item.getId()+" term #"+term.getUuid());
								}
								else if (LoadState.isMerge(state)) {
									logger.info("Merged change item #"+item.getId()+" term #"+term.getUuid());
								}
								else if (LoadState.isRedundant(state)) {
									logger.info("Redundant change item #"+item.getId()+" term #"+term.getUuid());
									redundant.add(term);
								}
							}
						}
						if (terms.size() == redundant.size()) {
							logger.info("Removing redundant history item #"+item.getId());
							commitHistoryStore.remove(item, ontology);
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
