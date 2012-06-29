package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
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

	private final Map<String, OntologyTaskManager> managers = new HashMap<String, OntologyTaskManager>();

	private final Map<String, ConfiguredOntology> configurations;
	private final Set<String> skipOntologies;

	@Inject
	ReloadingOntologyLoader(OntologyConfiguration configuration,
			IRIMapper iriMapper,
			OntologyCleaner cleaner,
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies,
			@Named("ReloadingOntologyLoaderPeriod") long period,
			@Named("ReloadingOntologyLoaderTimeUnit") TimeUnit unit)
	{
		super(iriMapper, cleaner);
		if (skipOntologies == null) {
			skipOntologies = Collections.emptySet();
		}
		this.skipOntologies = skipOntologies;
		configurations = configuration.getOntologyConfigurations();

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

	private synchronized void reloadOntologies() {
		for (OntologyTaskManager manager : managers.values()) {
			try {
				manager.updateManaged();
			} catch (InvalidManagedInstanceException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	@Override
	public synchronized List<OntologyTaskManager> getOntologies() {
		List<OntologyTaskManager> result = new ArrayList<OntologyTaskManager>();
		Set<String> existing = new HashSet<String>();
		for (String name : configurations.keySet()) {
			ConfiguredOntology configuredOntology = configurations.get(name);
			if (!existing.contains(configuredOntology.getUniqueName())) {
				existing.add(configuredOntology.getUniqueName());
				OntologyTaskManager manager = getManager(configuredOntology);
				if (manager != null) {
					result.add(manager);
				}
			}
		}
		return result;
	}

	@Override
	public synchronized OntologyTaskManager getOntology(ConfiguredOntology configuredOntology) {
		return getManager(configuredOntology);
	}

	private OntologyTaskManager getManager(final ConfiguredOntology configuredOntology)  {
		if (!hasRealInstance(configuredOntology)) {
			return null;
		}
		String uniqueName = configuredOntology.getUniqueName();
		OntologyTaskManager manager = managers.get(uniqueName);
		if (manager == null) {
			try {
				manager = new OntologyTaskManager(configuredOntology) {

					@Override
					protected OWLGraphWrapper createManaged() throws InstanceCreationException {
						return loadOntology(configuredOntology);
					}

					@Override
					protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) throws InstanceCreationException {
						return reloadOntology(configuredOntology, managed);
					}

					@Override
					protected void dispose(OWLGraphWrapper managed) {
						disposeResource(managed);
					}
					
				};
			} catch (InvalidManagedInstanceException exception) {
				throw new RuntimeException(exception);
			}
			managers.put(uniqueName, manager);
		}
		return manager;
	}

	private boolean hasRealInstance(ConfiguredOntology configuredOntology) {
		String name = configuredOntology.getUniqueName();
		if (skipOntologies.contains(name)) {
			return false;
		}
		return true;
	}

	private OWLGraphWrapper loadOntology(ConfiguredOntology ontology) {
		String uniqueName = ontology.getUniqueName();
		if (skipOntologies.contains(uniqueName)) {
			LOGGER.info("Skipping ontology: " + uniqueName);
			return null;
		}
		try {
			OWLGraphWrapper w = getResource(ontology, null);
			return w;
		} catch (UnknownOWLOntologyException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private OWLGraphWrapper reloadOntology(ConfiguredOntology ontology, OWLGraphWrapper oldGraph) {
		try {
			OWLGraphWrapper w = getResource(ontology, oldGraph);
			return w;
		} catch (UnknownOWLOntologyException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
}
