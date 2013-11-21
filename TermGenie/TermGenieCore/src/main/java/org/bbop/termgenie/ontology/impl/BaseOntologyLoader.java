package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.IRIMapper;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

public class BaseOntologyLoader {

	private static final Logger LOGGER = Logger.getLogger(BaseOntologyLoader.class);
	private final IRIMapper iriMapper;
	private final OWLDataFactory factory;
	
	private OWLOntologyManager manager;

	protected BaseOntologyLoader(IRIMapper iriMapper) {
		super();
		this.iriMapper = iriMapper;
		factory = OWLManager.getOWLDataFactory();
		manager = createNewManager(iriMapper, factory);
	}

	private static OWLOntologyManager createNewManager(IRIMapper iriMapper, OWLDataFactory factory) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager(factory);
		if (iriMapper != null) {
			manager.addIRIMapper(iriMapper);
		}
		manager.addOntologyLoaderListener(new OWLOntologyLoaderListener() {
			
			@Override
			public void startedLoadingOntology(LoadingStartedEvent event) {
				IRI id = event.getOntologyID().getOntologyIRI();
				IRI source = event.getDocumentIRI();
				
				StringBuilder sb = new StringBuilder("Start loading ontology: ");
				sb.append(id).append(" from ").append(source);
				LOGGER.info(sb.toString());
			}
			
			@Override
			public void finishedLoadingOntology(LoadingFinishedEvent event) {
				IRI id = event.getOntologyID().getOntologyIRI();
				IRI source = event.getDocumentIRI();
				
				StringBuilder sb = new StringBuilder("Finished loading ontology");
				sb.append(id).append(" from ").append(source);
				LOGGER.info(sb.toString());
			}
			
		});
		return manager;
	}

	protected synchronized OWLGraphWrapper getResource(Ontology ontology, OWLGraphWrapper update)
			throws OWLOntologyCreationException, IOException, UnknownOWLOntologyException, OBOFormatParserException
	{
		if (update != null) {
			disposeResource(update);
		}
		OWLGraphWrapper w = load(ontology, ontology.getSource(), ontology.getImportRewrites());
		if (w == null) {
			return null;
		}
		final List<String> supports = ontology.getAdditionals();
		if (supports != null) {
			for (String support : supports) {
				OWLOntology owl = loadOntology("support", support, ontology.getImportRewrites());
				if (owl != null) {
					w.addSupportOntology(owl);
					w.mergeOntology(owl);
				}
			}
		}
		w.addSupportOntologiesFromImportsClosure();
		
		// throws UnknownOWLOntologyException
		w.getAllOntologies();
		
		return w;
	}

	protected void disposeResource(OWLGraphWrapper graph) throws UnknownOWLOntologyException {
		Set<OWLOntology> closure = graph.getSourceOntology().getImportsClosure();
		for (OWLOntology owlOntology : closure) {
			manager.removeOntology(owlOntology);	
		}
		Set<OWLOntology> supports = graph.getSupportOntologySet();
		if (supports != null) {
			for (OWLOntology support : supports) {
				manager.removeOntology(support);
			}
		}
		manager = createNewManager(iriMapper, factory);
	}

	protected OWLGraphWrapper load(Ontology ontology, String url, Map<String, String> importRewrites)
			throws OWLOntologyCreationException, IOException, OBOFormatParserException
	{
		OWLOntology owlOntology = loadOntology(ontology.getName(), url, importRewrites);
		if (owlOntology == null) {
			return null;
		}
		return new OWLGraphWrapper(owlOntology);
	}

	protected OWLOntology loadOntology(String ontology, String url, Map<String, String> importRewrites)
			throws OWLOntologyCreationException, IOException, OBOFormatParserException
	{
		LOGGER.info("Loading ontology: " + ontology + "  baseURL: " + url);
		URL realUrl;
		if (iriMapper != null) {
			realUrl = iriMapper.mapUrl(url);
		}
		else {
			realUrl = new URL(url);
		}
		String path = realUrl.getPath();
		String query = realUrl.getQuery();
		if ((path != null && path.endsWith(".obo")) || (query != null &&  query.endsWith(".obo"))) {
			return loadOBO2OWL(ontology, realUrl, importRewrites);
		}
		else if ((path != null && path.endsWith(".owl")) || (query != null && query.endsWith(".owl"))) {
			return loadOWLPure(ontology, url, realUrl, importRewrites);
		}
		else {
			throw new RuntimeException("Unable to load ontology from url, as no known suffix ('.obo' or '.owl') was detected: " + url);
		}
	}

	/**
	 * Load an owl ontology from a given URL.
	 * 
	 * @param ontology
	 * @param original
	 * @param realUrl
	 * @param importRewrites 
	 * @return OWLOntology
	 * @throws OWLOntologyCreationException
	 */
	protected OWLOntology loadOWLPure(String ontology, String original, URL realUrl, Map<String, String> importRewrites) throws OWLOntologyCreationException {
		OWLOntology ont;
		try {
			// Use the original as IRI,
			// the OWL-API use the IRIMapper to resolve it
			IRI iri = IRI.create(original);
			ont = manager.loadOntology(iri);
		} catch (OWLOntologyAlreadyExistsException exception) {
			// Trying to recover from exception
			ont = handleException(exception);
		} catch (OWLOntologyDocumentAlreadyExistsException exception) {
			// Trying to recover from exception
			ont = handleException(ontology, exception);
		}
		postProcessOWLOntology(ontology, ont);
		return ont;
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
	 * @throws OBOFormatParserException 
	 */
	protected OWLOntology loadOBO2OWL(String ontology, URL realUrl, Map<String, String> importRewrites)
			throws IOException, OWLOntologyCreationException, OBOFormatParserException
	{
		OBODoc obodoc = loadOBO(ontology, realUrl, importRewrites);
		
		Obo2Owl obo2Owl = new Obo2Owl(manager);
		
		LOGGER.info("Convert ontology " + ontology + " to owl.");
		OWLOntology ont;
		try {
			ont = obo2Owl.convert(obodoc, false);
		} catch (OWLOntologyAlreadyExistsException exception) {
			// Trying to recover from exception
			ont = handleException(exception);
		} catch (OWLOntologyDocumentAlreadyExistsException exception) {
			// Trying to recover from exception
			ont = handleException(ontology, exception);
		}
		LOGGER.info("Finished loading ontology: " + ontology);
		return ont;
	}

	protected OBODoc loadOBO(String ontology, URL realUrl, Map<String, String> importRewrites) throws IOException, OBOFormatParserException {
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc;
		try {
			LOGGER.info("Start parsing '"+ontology+"' obo ontology from input: " + realUrl);
			obodoc = p.parse(realUrl);
			if (obodoc == null) {
				throw new RuntimeException("Could not load '"+ontology+"': " + realUrl);
			}
			if (importRewrites != null && !importRewrites.isEmpty()) {
				Frame headerFrame = obodoc.getHeaderFrame();
				if (headerFrame != null) {
					Collection<Clause> clauses = headerFrame.getClauses(OboFormatTag.TAG_IMPORT);
					if (clauses != null && !clauses.isEmpty()) {
						checkImportRewrites(headerFrame, clauses, importRewrites);
					}
				}
			}
			postProcessOBOOntology(ontology, obodoc);
		} catch (StringIndexOutOfBoundsException exception) {
			LOGGER.warn("Error parsing input: " + realUrl);
			throw exception;
		}
		return obodoc;
	}
	
	private void checkImportRewrites(Frame frame, Collection<Clause> importClauses, Map<String, String> importRewrites) {
		boolean replace = false;
		List<Clause> changed = new ArrayList<Clause>();
		for (Clause importClause : importClauses) {
			String importDecl = importClause.getValue(String.class);
			String replacement = importRewrites.get(importDecl);
			if (replacement != null) {
				replace = true;
				changed.add(new Clause(OboFormatTag.TAG_IMPORT, replacement));
			}
			else {
				changed.add(importClause);
			}
		}
		
		if (replace) {
			frame.getClauses().removeAll(importClauses);
			frame.getClauses().addAll(changed);
		}
	}
	
	/**
	 * Method called for post processing of obo ontologies after the load.
	 * 
	 * @param ontology
	 * @param obodoc
	 */
	protected void postProcessOBOOntology(String ontology, OBODoc obodoc) {
		// intentionally empty
	}

	private OWLOntology handleException(String ontology,
			OWLOntologyDocumentAlreadyExistsException exception)
			throws OWLOntologyDocumentAlreadyExistsException
	{
		OWLOntology ont;
		IRI duplicate = exception.getOntologyDocumentIRI();
		ont = manager.getOntology(duplicate);
		if (ont == null) {
			for(OWLOntology managed : manager.getOntologies()) {
				if(duplicate.equals(managed.getOntologyID().getOntologyIRI())) {
					LOGGER.info("Skip already loaded ontology: "+ontology);
					ont = managed;
					break;
				}
			}
		}
		if (ont == null) {
			// throw original exception, if no ontology could be found
			// never return null ontology
			throw exception;
		}
		return ont;
	}

	private OWLOntology handleException(OWLOntologyAlreadyExistsException exception)
			throws OWLOntologyAlreadyExistsException
	{
		OWLOntology ont;
		OWLOntologyID duplicate = exception.getOntologyID();
		ont = manager.getOntology(duplicate);
		if (ont == null) {
			// throw original exception, if no ontology could be found
			// never return null ontology
			throw exception;
		}
		return ont;
	}

}
