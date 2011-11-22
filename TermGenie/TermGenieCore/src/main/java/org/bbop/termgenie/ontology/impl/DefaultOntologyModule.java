package org.bbop.termgenie.ontology.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Abstract module providing the default ontologies.
 * 
 * @see OntologyLoader
 * @see MultiOntologyTaskManager
 * @see OntologyConfiguration
 * @see OntologyCleaner
 * @see IRIMapper
 */
public abstract class DefaultOntologyModule extends IOCModule {

	protected final String defaultOntologyConfigurationResource;
	protected final String defaultOntologyCleanerResource;

	public DefaultOntologyModule(Properties applicationProperties) {
		this(DefaultOntologyConfiguration.SETTINGS_FILE, DefaultOntologyCleaner.SETTINGS_FILE, applicationProperties);
	}

	public DefaultOntologyModule(String ontologyConfigurationResource,
			String ontologyCleanerResource,
			Properties applicationProperties)
	{
		super(applicationProperties);
		this.defaultOntologyConfigurationResource = ontologyConfigurationResource;
		this.defaultOntologyCleanerResource = ontologyCleanerResource;
	}

	@Override
	protected void configure() {
		bindOntologyLoader();

		bindOntologyConfiguration();

		bindOntologyCleaner();

		bindIRIMapper();
	}

	protected void bindOntologyLoader() {
		bind(OntologyLoader.class, DefaultOntologyLoader.class);
	}

	@Provides
	@Singleton
	@Named("DefaultOntologyLoaderSkipOntologies")
	protected Set<String> getDefaultOntologyLoaderSkipOntologies() {
		return new HashSet<String>(Arrays.asList("HumanPhenotype", "FMA", "PATO", "OMP", "CL"));
	}

	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class, DefaultOntologyConfiguration.class);
		bind("DefaultOntologyConfigurationResource", defaultOntologyConfigurationResource);
	}

	protected void bindOntologyCleaner() {
		bind(OntologyCleaner.class, DefaultOntologyCleaner.class);
		bind("DefaultOntologyCleanerResource", defaultOntologyCleanerResource);
	}

	protected abstract void bindIRIMapper();

	@Provides
	@Singleton
	MultiOntologyTaskManager provideMultiOntologyTaskManager(OntologyLoader loader) {

		List<OntologyTaskManager> ontologies = loader.getOntologies();
		return new ManagedMultiOntologyTaskManager(ontologies);
	}

	private static class ManagedMultiOntologyTaskManager extends MultiOntologyTaskManager {

		ManagedMultiOntologyTaskManager(List<OntologyTaskManager> ontologies) {
			super("ManagedMultiOntologyTaskManager", ontologies);
		}
	}
}
