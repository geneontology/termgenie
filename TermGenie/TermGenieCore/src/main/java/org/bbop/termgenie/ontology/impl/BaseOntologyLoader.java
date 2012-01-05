package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owltools.graph.OWLGraphWrapper;

public class BaseOntologyLoader {

	private static final Logger LOGGER = Logger.getLogger(BaseOntologyLoader.class);
	private final IRIMapper iriMapper;
	private final OntologyCleaner cleaner;

	protected BaseOntologyLoader(IRIMapper iriMapper, OntologyCleaner cleaner) {
		super();
		this.iriMapper = iriMapper;
		this.cleaner = cleaner;
	}

	protected OWLGraphWrapper getResource(ConfiguredOntology ontology)
			throws OWLOntologyCreationException, IOException
	{
		OWLGraphWrapper w = load(ontology, ontology.source);
		if (w == null) {
			return null;
		}
		for (String support : ontology.getSupports()) {
			OWLOntology owl = loadOntology("support", support);
			if (support != null) {
				w.addSupportOntology(owl);
			}
		}
		return w;
	}

	protected OWLGraphWrapper load(Ontology ontology, String url)
			throws OWLOntologyCreationException, IOException
	{
		OWLOntology owlOntology = loadOntology(ontology.getUniqueName(), url);
		if (owlOntology == null) {
			return null;
		}
		return new OWLGraphWrapper(owlOntology);
	}

	protected OWLOntology loadOntology(String ontology, String url)
			throws OWLOntologyCreationException, IOException
	{
		LOGGER.info("Loading ontology: " + ontology + "  baseURL: " + url);
		URL realUrl;
		if (iriMapper != null) {
			realUrl = iriMapper.mapUrl(url);
		}
		else {
			realUrl = new URL(url);
		}
		if (realUrl.getPath().endsWith(".obo") || realUrl.getQuery().endsWith(".obo")) {
			return loadOBO2OWL(ontology, realUrl);
		}
		else if (realUrl.getPath().endsWith(".owl") || realUrl.getQuery().endsWith(".owl")) {
			return loadOWLPure(ontology, realUrl);
		}
		else {
			throw new RuntimeException("Unable to load ontology from url, as no known suffix ('.obo' or '.owl') was detected: " + url);
		}
	}

	/**
	 * Load an owl ontology from a given URL.
	 * 
	 * @param ontology
	 * @param realUrl
	 * @return OWLOntology
	 * @throws OWLOntologyCreationException
	 */
	protected OWLOntology loadOWLPure(String ontology, URL realUrl) throws OWLOntologyCreationException {
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(IRI.create(realUrl));
			postProcessOWLOntology(ontology, owlOntology);
			return owlOntology;
		} catch (URISyntaxException exception) {
			throw new OWLOntologyCreationException(exception);
		}
	}
	
	/**
	 * Method called for post processing of owl ontologies after the load.
	 * 
	 * @param ontology
	 * @param owlOntology
	 */
	protected void postProcessOWLOntology(String ontology, OWLOntology owlOntology) {
		// do nothing
	}

	/**
	 * Load an OBO ontology and convert it to OWL.
	 * 
	 * @param ontology
	 * @param realUrl
	 * @return OWLOntology
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	 */
	protected OWLOntology loadOBO2OWL(String ontology, URL realUrl)
			throws IOException, OWLOntologyCreationException
	{
		OBODoc obodoc = loadOBO(ontology, realUrl);
		
		Obo2Owl obo2Owl = new Obo2Owl();
		LOGGER.info("Convert ontology " + ontology + " to owl.");
		OWLOntology owlOntology = obo2Owl.convert(obodoc);
		LOGGER.info("Finished loading ontology: " + ontology);
		return owlOntology;
	}

	protected OBODoc loadOBO(String ontology, URL realUrl) throws IOException {
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc;
		try {
			LOGGER.info("Start parsing obo ontology from input: " + realUrl);
			obodoc = p.parse(realUrl);
			if (obodoc == null) {
				throw new RuntimeException("Could not load: " + realUrl);
			}
			postProcessOBOOntology(ontology, obodoc);
		} catch (StringIndexOutOfBoundsException exception) {
			LOGGER.warn("Error parsing input: " + realUrl);
			throw exception;
		}
		return obodoc;
	}
	
	/**
	 * Method called for post processing of obo ontologies after the load.
	 * 
	 * @param ontology
	 * @param obodoc
	 */
	protected void postProcessOBOOntology(String ontology, OBODoc obodoc) {
		cleaner.cleanOBOOntology(ontology, obodoc);
	}

}
