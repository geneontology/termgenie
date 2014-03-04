package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Load ontologies into memory and reload them from the source periodically.
 * Changed ontologies are recovered, by reloading the ontology from the source.
 * The source and local copy of an ontology are controlled by the configured
 * {@link OWLOntologyIRIMapper}.
 */
@Singleton
class ReloadingOntologyLoader extends BaseOntologyLoader implements OntologyLoader {

	protected final static Logger LOGGER = Logger.getLogger(ReloadingOntologyLoader.class);

	private final OntologyTaskManager manager;

	@Inject
	ReloadingOntologyLoader(OntologyConfiguration configuration,
			@Named("IRIMappers") List<OWLOntologyIRIMapper> iriMappers,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit)
	{
		super(iriMappers);
		try {
			Ontology ontology = configuration.getOntologyConfiguration();
			manager = new ReloadingOntologyTaskManager(ontology);
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		}
		// use invalid settings to de-activate the reloading
		if (period > 0 && unit != null) {
			// use java.concurrent to schedule periodic task of reloading the
			// ontologies.
			Runnable command = new Runnable() {

				@Override
				public void run() {
					LOGGER.info("Scheduled Event - Start reloading ontologies");
					reloadOntologies();
				}
			};
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleWithFixedDelay(command, period, period, unit);
		}
		else {
			LOGGER.warn("Ontology Reloading is deactivated, due to invalid settings: period="+period+" unit="+unit);
		}
	}

	@Override
	public synchronized void reloadOntologies() {
		try {
			manager.updateManaged();
		} catch (InvalidManagedInstanceException first) {
			LOGGER.info("Trying to recover an invalid ontology state during ontology reload.");
			manager.recoverInvalid();
		}
	}

	@Override
	public synchronized OntologyTaskManager getOntologyManager() {
		return manager;
	}

	private OWLGraphWrapper loadOntology(Ontology ontology) {
		try {
			OWLGraphWrapper w = getResource(ontology);
			return w;
		} catch (UnknownOWLOntologyException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (OBOFormatParserException exception) {
			throw new RuntimeException(exception);
		}
	}

	private final class ReloadingOntologyTaskManager extends OntologyTaskManager {
	
		private ReloadingOntologyTaskManager(Ontology ontology) throws InvalidManagedInstanceException
		{
			super(ontology);
		}
	
		@Override
		protected OWLGraphWrapper createManaged() throws InstanceCreationException {
			OWLGraphWrapper graph = loadOntology(getOntology());
			return graph;
		}
	
		@Override
		protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) throws InstanceCreationException {
			// do not load the ontology immediately
			// this is a blocking call, while returning the ontology after the commit
			disposeOntologies();
			return null;
		}
	
		@Override
		protected void dispose(OWLGraphWrapper managed) {
			disposeOntologies();
		}
	
	}
	
}
