package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Load ontologies into memory and reload them from the source periodically.
 * Changed ontologies are recovered, by reloading the ontology from the source.
 * The source and local copy of an ontology are controlled by the configured
 * {@link IRIMapper}.
 */
@Singleton
public class ReloadingOntologyLoader extends BaseOntologyLoader implements OntologyLoader {

	protected final static Logger LOGGER = Logger.getLogger(ReloadingOntologyLoader.class);

	private final OntologyTaskManager manager;

	@Inject
	ReloadingOntologyLoader(OntologyConfiguration configuration,
			IRIMapper iriMapper,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit)
	{
		super(iriMapper);
		try {
			manager = new ReloadingOntologyTaskManager(configuration.getOntologyConfiguration());
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

	public synchronized void reloadOntologies() {
		try {
			manager.updateManaged();
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public synchronized OntologyTaskManager getOntologyManager() {
		return manager;
	}

	private OWLGraphWrapper loadOntology(Ontology ontology) {
		try {
			OWLGraphWrapper w = getResource(ontology, null);
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

	private OWLGraphWrapper reloadOntology(Ontology ontology, OWLGraphWrapper oldGraph) {
		try {
			OWLGraphWrapper w = getResource(ontology, oldGraph);
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
	
//		private static final int MAX_RETRY_COUNT = 3;
//		int currentRetryCount = 0;

		private ReloadingOntologyTaskManager(Ontology ontology) throws InvalidManagedInstanceException
		{
			super(ontology);
		}
	
		@Override
		protected OWLGraphWrapper createManaged() throws InstanceCreationException {
			OWLGraphWrapper graph = loadOntology(getOntology());
//			currentRetryCount = 0;
			return graph;
		}
	
		@Override
		protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) throws InstanceCreationException {
			OWLGraphWrapper graph = reloadOntology(getOntology(), managed);
//			currentRetryCount = 0;
			return graph;
		}
	
		@Override
		protected void dispose(OWLGraphWrapper managed) {
			disposeResource(managed);
		}
	
//		@Override
//		protected OWLGraphWrapper handleInvalid(OWLGraphWrapper managed) throws InvalidManagedInstanceException {
//			if (currentRetryCount < MAX_RETRY_COUNT) {
//				LOGGER.info("Trying to recover from an invalid state. This number of previous attempts: "+currentRetryCount);
//				disposeResource(managed);
//				currentRetryCount += 1;
//				return null;
//			}
//			return super.handleInvalid(managed);
//		}
	}
	
}
