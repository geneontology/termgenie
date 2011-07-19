package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration.ConfiguredOntology;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DefaultOntologyLoader implements OntologyLoader {
	
	private final static Logger LOGGER = Logger.getLogger(DefaultOntologyLoader.class);
	
	private final Map<String, OntologyTaskManager> managers = new HashMap<String, OntologyTaskManager>();
	private final Map<String, OWLGraphWrapper> ontologies = new HashMap<String, OWLGraphWrapper>();
	
	private final IRIMapper localFileIRIMapper;
	private final OntologyCleaner cleaner;
	private final Map<String, ConfiguredOntology> configurations;
	private final Set<String> skipOntologies;
	
	@Inject
	DefaultOntologyLoader(OntologyConfiguration configuration, 
			IRIMapper localFileIRIMapper, 
			OntologyCleaner cleaner, 
			@Named("DefaultOntologyLoaderSkipOntologies") Set<String> skipOntologies) {
		super();
		this.localFileIRIMapper = localFileIRIMapper;
		this.cleaner = cleaner;
		this.skipOntologies = skipOntologies;
		configurations = configuration.getOntologyConfigurations();
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
	
	private OntologyTaskManager getManager(final ConfiguredOntology configuredOntology) {
		if (!hasRealInstance(configuredOntology)) {
			return null;
		}
		String uniqueName = configuredOntology.getUniqueName();
		OntologyTaskManager manager = managers.get(uniqueName);
		if (manager == null) {
			manager = new OntologyTaskManager(configuredOntology) {
				
				@Override
				protected OWLGraphWrapper createManaged() {
					return loadOntology(configuredOntology);
				}
			};
			managers.put(uniqueName, manager);
		}
		return manager;
	}
	
	private boolean hasRealInstance(ConfiguredOntology configuredOntology) {
		String name = configuredOntology.getUniqueName();
		if (skipOntologies.contains(name)) {
			LOGGER.info("Skipping ontology: "+name);
			return false;
		}
		return true;
	}
	
	private OWLGraphWrapper loadOntology(ConfiguredOntology ontology) {
		String uniqueName = ontology.getUniqueName();
		if (ontologies.containsKey(uniqueName)) {
			return ontologies.get(uniqueName);
		}
		try {
			OWLGraphWrapper w = getResource(ontology);
			ontologies.put(uniqueName, w);
			return w;
		} catch (UnknownOWLOntologyException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}


	protected OWLGraphWrapper getResource(ConfiguredOntology ontology) throws OWLOntologyCreationException, IOException {
		OWLGraphWrapper w = load(ontology, ontology.source);
		if (w == null) {
			return null;
		}
		for(String support : ontology.getSupports()) {
			OWLOntology owl = loadOWL("support", support);
			if (support != null) {
				w.addSupportOntology(owl);
			}
		}
		return w;
	}
	
	OWLGraphWrapper load(Ontology ontology, String url) throws OWLOntologyCreationException, IOException {
		OWLOntology owlOntology = loadOWL(ontology.getUniqueName(), url);
		if (owlOntology == null) {
			return null;
		}
		return new OWLGraphWrapper(owlOntology);
	}

	
	
	protected OWLOntology loadOWL(String ontology, String url) throws OWLOntologyCreationException, IOException {
		if (skipOntologies.contains(ontology)) {
			LOGGER.info("Skipping ontology: "+ontology);
			return null;
		}
		LOGGER.info("Loading ontology: "+ontology+"  baseURL: "+url);
		URL realUrl;
		if (localFileIRIMapper != null) {
			realUrl = localFileIRIMapper.mapUrl(url);
		}
		else {
			realUrl = new URL(url);
		}
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc;
		try {
			LOGGER.info("Start parsing obo ontology from input: "+realUrl);
			obodoc = p.parse(realUrl);
			if (obodoc == null) {
				throw new RuntimeException("Could not load: "+realUrl);
			}
			cleaner.cleanOBOOntology(ontology, obodoc);
		} catch (StringIndexOutOfBoundsException exception) {
			LOGGER.warn("Error parsing input: "+realUrl);
			throw exception;
		}
		Obo2Owl obo2Owl = new Obo2Owl();
		LOGGER.info("Convert ontology "+ontology+" to owl.");
		OWLOntology owlOntology = obo2Owl.convert(obodoc);
		LOGGER.info("Finished loading ontology: "+ontology);
		return owlOntology;
	}

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new DefaultOntologyModule());
		OntologyLoader loader = injector.getInstance(OntologyLoader.class);
		List<OntologyTaskManager> ontologies = loader.getOntologies();
		
		OntologyTaskManager ontologyTaskManager = ontologies.get(0);
		ontologyTaskManager.runManagedTask(new OntologyTask(){

			@Override
			public void run(OWLGraphWrapper ontology) {
				OWLObject owlObject = ontology.getOWLObjectByIdentifier("GO:0003674");
				System.out.println(owlObject);
				System.out.println(ontology.getLabel(owlObject));
			}
		});
		
	}
}