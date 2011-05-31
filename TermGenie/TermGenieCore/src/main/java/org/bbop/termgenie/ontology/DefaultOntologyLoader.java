package org.bbop.termgenie.ontology;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration.ConfiguredOntology;
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
	
	private final Map<String, OWLGraphWrapper> ontologies = new HashMap<String, OWLGraphWrapper>();
	private final LocalFileIRIMapper localFileIRIMapper;

	public DefaultOntologyLoader() {
		super();
		localFileIRIMapper = new LocalFileIRIMapper();
	}

	public List<Ontology> getOntologies() {
		Map<String, ConfiguredOntology> configuration = DefaultOntologyConfiguration.getOntologies();
		List<Ontology> result = new ArrayList<Ontology>();
		for (String name : configuration.keySet()) {
			ConfiguredOntology configuredOntology = configuration.get(name);
			OWLGraphWrapper realInstance = loadOntology(configuredOntology);
			if (realInstance != null) {
				result.add(configuredOntology.createOntology(realInstance));
			}
		}
		return result;
	}
	
	public OWLGraphWrapper getOntology(ConfiguredOntology ontology) {
		OWLGraphWrapper realInstance = loadOntology(ontology);
		return realInstance;
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
		LOGGER.info("Loading ontology: "+ontology+"  baseURL: "+url);
		URL realUrl = localFileIRIMapper.getUrl(url);
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc;
		try {
			LOGGER.info("Start parsing obo ontology from input: "+realUrl);
			obodoc = p.parse(realUrl);
			if (obodoc == null) {
				throw new RuntimeException("Could not load: "+realUrl);
			}
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
		DefaultOntologyLoader instance = new DefaultOntologyLoader();
		List<Ontology> ontologies = instance.getOntologies();
		
		OWLGraphWrapper ontology = ontologies.get(0).getRealInstance();
		OWLObject owlObject = ontology.getOWLObjectByIdentifier("GO:0003674");
		System.out.println(owlObject);
	}
}