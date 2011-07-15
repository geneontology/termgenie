package org.bbop.termgenie.ontology;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

public class DefaultOntologyLoader {
	
	private final static Logger LOGGER = Logger.getLogger(DefaultOntologyLoader.class);
	
	private static volatile DefaultOntologyLoader instance = null;
	
	// if there are multiple overlapping managers a deadlock may occur!
	// --> enforce the singleton for manager in the ontology loader 
	private volatile MultiOntologyTaskManager globalManager = null;
	private final Map<String, OntologyTaskManager> managers = new HashMap<String, OntologyTaskManager>();
	private final Map<String, OWLGraphWrapper> ontologies = new HashMap<String, OWLGraphWrapper>();
	private final IRIMapper localFileIRIMapper;

	private DefaultOntologyLoader(IRIMapper localFileIRIMapper) {
		super();
		this.localFileIRIMapper = localFileIRIMapper;
	}

	synchronized static DefaultOntologyLoader getInstance() {
		if (instance == null) {
			instance = new DefaultOntologyLoader(DefaultOntologyConfiguration.getIRIMapper());
		}
		return instance;
	}
	
	/**
	 * Load all configured ontologies.
	 * 
	 * @return ontologies
	 */
	public synchronized static List<OntologyTaskManager> getOntologies() {
		Map<String, ConfiguredOntology> configuration = DefaultOntologyConfiguration.getOntologies();
		List<OntologyTaskManager> result = new ArrayList<OntologyTaskManager>();
		Set<String> existing = new HashSet<String>(); 
		for (String name : configuration.keySet()) {
			ConfiguredOntology configuredOntology = configuration.get(name);
			if (!existing.contains(configuredOntology.getUniqueName())) {
				existing.add(configuredOntology.getUniqueName());
				OntologyTaskManager manager = getInstance().getManager(configuredOntology);
				if (manager != null) {
					result.add(manager);
				}
				
			}
		}
		return result;
	}
	
	/**
	 * Load a selected ontology configuration, useful for testing.
	 * 
	 * @param configuredOntology parameter
	 * @return ontology
	 */
	public synchronized static OntologyTaskManager getOntology(final ConfiguredOntology configuredOntology) {
		return getInstance().getManager(configuredOntology);
	}
	
	public synchronized static MultiOntologyTaskManager getMultiOntologyManager() {
		DefaultOntologyLoader instance = getInstance();
		if (instance.globalManager == null) {
			List<OntologyTaskManager> ontologies = getOntologies();
			instance.globalManager = new MultiOntologyTaskManager(ontologies);
		}
		return instance.globalManager;
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
	
	private static Set<String> skipOntologies = new HashSet<String>(Arrays.asList("HumanPhenotype","FMA","PATO", "OMP", "CL"));
	
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
			DefaultOntologyCleaner.cleanOntology(ontology, obodoc);
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
		List<OntologyTaskManager> ontologies = getOntologies();
		
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