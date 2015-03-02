package org.bbop.termgenie.ontology.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

public class BaseOntologyLoader {
	
	private static final Logger logger = Logger.getLogger(BaseOntologyLoader.class);

	private ParserWrapper pw;

	protected BaseOntologyLoader(List<OWLOntologyIRIMapper> iriMappers) {
		super();
		pw = new ParserWrapper();
		if (iriMappers != null) {
			for (OWLOntologyIRIMapper iriMapper : iriMappers) {
				pw.addIRIMapper(iriMapper);
			}
		}
	}

	protected synchronized OWLGraphWrapper getResource(Ontology ontology)
			throws Throwable
	{
		OWLGraphWrapper w;
		try {
			w = load(ontology.getSource());
		} catch (Throwable exception) {
			logger.error("Could not load ontology: "+ontology.getSource(), exception);
			throw exception;
		}
		
		if (w == null) {
			logger.error("Returned null ontology for: "+ontology.getSource());
			throw new NullPointerException("An ontology should never be null: "+ontology.getSource());
		}
		final List<String> supports = ontology.getAdditionals();
		if (supports != null) {
			for (String support : supports) {
				OWLOntology owl;
				try {
					owl = loadOwl(support);
				} catch (Throwable exception) {
					logger.error("Could not load support ontology: "+support, exception);
					throw exception;
				}
				if (owl != null) {
					try {
						w.mergeOntology(owl);
					} catch (Throwable exception) {
						logger.error("Could not merge support ("+support+") into main ontology", exception);
						throw exception;
					}
				}
				else {
					logger.error("Could not load support ("+support+"), loaded ontology is null");
					throw new NullPointerException("An ontology should never be null: "+support);
				}
			}
		}
		
		// throws UnknownOWLOntologyException
		try {
			w.getAllOntologies();
		} catch (UnknownOWLOntologyException exception) {
			logger.error("Error during the load of ontologies.", exception);
			throw exception;
		}
		
		return w;
	}

	protected synchronized void disposeOntologies() {
		// WARNING: this is also called for invalid states
		// always use a clean new Wrapper
		// there are still issues with cleaning a manager from all previous ontologies
		
		// Step 1: try to clean up existing manager
		List<OWLOntologyIRIMapper> mappers = new ArrayList<OWLOntologyIRIMapper>(pw.getIRIMappers());
		OWLOntologyManager oldManager = pw.getManager();
		Set<OWLOntology> ontologies = oldManager.getOntologies();
		for (OWLOntology ontology : ontologies) {
			oldManager.removeOntology(ontology);
		}
		oldManager.clearIRIMappers();
		
		// Step 2: create a new wrapper and transfer IRI mappers
		ParserWrapper newWrapper = new ParserWrapper();
		newWrapper.addIRIMappers(mappers);
		pw = newWrapper;
	}

	protected OWLGraphWrapper load(String url) throws Exception {
		OWLOntology owlOntology = loadOwl(url);
		if (owlOntology == null) {
			return null;
		}
		return new OWLGraphWrapper(owlOntology);
	}
	
	protected OWLOntology loadOwl(String url) throws Exception {
		return pw.parse(url);
	}

}
