package org.bbop.termgenie.ontology.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Module providing the default ontologies.
 * 
 * @see OntologyLoader
 * @see MultiOntologyTaskManager
 * @see OntologyConfiguration
 * @see OntologyCleaner
 * @see IRIMapper
 */
public class DefaultOntologyModule extends IOCModule {

	protected final String defaultOntologyConfigurationResource;
	protected final String defaultOntologyCleanerResource;
	protected final String localFileIRIMapperResource;

	public DefaultOntologyModule() {
		this(DefaultOntologyConfiguration.SETTINGS_FILE, DefaultOntologyCleaner.SETTINGS_FILE, LocalFileIRIMapper.SETTINGS_FILE);
	}

	public DefaultOntologyModule(String ontologyConfigurationResource,
			String ontologyCleanerResource,
			String localFileIRIMapperResource)
	{
		this.defaultOntologyConfigurationResource = ontologyConfigurationResource;
		this.defaultOntologyCleanerResource = ontologyCleanerResource;
		this.localFileIRIMapperResource = localFileIRIMapperResource;
	}

	@Override
	protected void configure() {
		bindOntologyLoader();

		bindOntologyConfiguration();

		bindOntologyCleaner();

		bindIRIMapper();
	}

	protected void bindOntologyLoader() {
		bind(OntologyLoader.class).to(DefaultOntologyLoader.class);
	}

	@Provides
	@Singleton
	@Named("DefaultOntologyLoaderSkipOntologies")
	protected Set<String> getDefaultOntologyLoaderSkipOntologies() {
		return new HashSet<String>(Arrays.asList("HumanPhenotype", "FMA", "PATO", "OMP", "CL"));
	}

	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class).to(DefaultOntologyConfiguration.class);
		bind("DefaultOntologyConfigurationResource", defaultOntologyConfigurationResource);
	}

	protected void bindOntologyCleaner() {
		bind(OntologyCleaner.class).to(DefaultOntologyCleaner.class);
		bind("DefaultOntologyCleanerResource", defaultOntologyCleanerResource);
	}

	protected void bindIRIMapper() {
		bind(IRIMapper.class).to(LocalFileIRIMapper.class);
		bind("LocalFileIRIMapperResource", localFileIRIMapperResource);
	}

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

	/**
	 * Main method for a quick test of the config (error free startup).
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Injector injector = TermGenieGuice.createInjector(new DefaultOntologyModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		MultiOntologyTaskManager manager = injector.getInstance(MultiOntologyTaskManager.class);
		MultiOntologyTask task = new MultiOntologyTask() {

			@Override
			public List<Modified> run(List<OWLGraphWrapper> requested) {
				System.out.println("requested: " + requested.size());
				return null;
			}
		};
		manager.runManagedTask(task, configuration.getOntologyConfigurations().get("GeneOntology"));

	}

}
