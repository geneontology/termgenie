package org.bbop.termgenie.ontology.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.ScmHelper;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owltools.graph.OWLGraphWrapper;
import owltools.io.CatalogXmlIRIMapper;

public abstract class OwlScmHelper extends ScmHelper<OWLOntology> {

	private static final Logger LOG = Logger.getLogger(OwlScmHelper.class);
	
	private final String catalogXml;

	protected OwlScmHelper(String svnOntologyFileName, List<OWLOntologyIRIMapper> defaultMappers, String catalogXml)
	{
		super(svnOntologyFileName, defaultMappers);
		this.catalogXml = catalogXml;
	}

	@Override
	public boolean applyHistoryChanges(ScmCommitData data,
			List<CommitedOntologyTerm> terms,
			OWLOntology ontology) throws CommitException
	{
		try {
			if (terms != null && !terms.isEmpty()) {
				OWLOntologyManager manager = ontology.getOWLOntologyManager();
				// apply changes to the ontology 
				for (CommitedOntologyTerm term : terms) {
					Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(term.getAxioms());
					manager.addAxioms(ontology, axioms);
					
					List<SimpleCommitedOntologyTerm> changed = term.getChanged();
					for (SimpleCommitedOntologyTerm changedTerm : changed) {
						Set<OWLAxiom> changedAxioms = OwlStringTools.translateStringToAxioms(changedTerm.getAxioms());
						manager.addAxioms(ontology, changedAxioms);
					}
				}
			}
			return true;
		} catch (OWLOntologyChangeException exception) {
			String message = "Could not apply changes to ontology";
			throw error(message, exception, true);
		}
	}

	@Override
	public void createModifiedTargetFile(ScmCommitData data,
			OWLOntology ontology,
			OWLGraphWrapper graph,
			String savedBy) throws CommitException
	{
		// write changed ontology to file
		final File modifiedTargetFile = data.getModifiedTargetFile();
		OutputStream outputStream = null;
		try {
			// check that all folders are created
			modifiedTargetFile.getParentFile().mkdirs();
			
			// write OWL file
			outputStream = new FileOutputStream(modifiedTargetFile);
			OWLOntologyManager manager = ontology.getOWLOntologyManager();
			manager.saveOntology(ontology, outputStream);
		} catch (Exception exception) {
			String message = "Could not write ontology changes to file";
			throw error(message, exception, true);
		}
		finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	@Override
	protected OWLOntology loadOntology(File scmFile, ScmCommitData data, List<OWLOntologyIRIMapper> defaultMappers) throws CommitException {
		// use a new manager to avoid 'already loaded' exceptions
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// register a listener for logging
		manager.addOntologyLoaderListener(new OWLOntologyLoaderListener() {

			// generated
			private static final long serialVersionUID = -3636618634133069889L;

			@Override
			public void startedLoadingOntology(LoadingStartedEvent event) {
				IRI id = event.getOntologyID().getOntologyIRI().orNull();
				IRI source = event.getDocumentIRI();
				LOG.info("Start loading from SCM for commit, id: "+id+" source: "+source);
				
			}
			
			@Override
			public void finishedLoadingOntology(LoadingFinishedEvent event) {
				IRI id = event.getOntologyID().getOntologyIRI().orNull();
				IRI source = event.getDocumentIRI();
				LOG.info("Finished loading from SCM for commit, id: "+id+" source: "+source);
			}
		});
		if(defaultMappers != null) {
			for (OWLOntologyIRIMapper iriMapper : defaultMappers) {
				manager.getIRIMappers().add(iriMapper);
			}
		}
		if (catalogXml != null) {
			File catalogFile = new File(data.getScmFolder(), catalogXml).getAbsoluteFile();
			try {
				manager.getIRIMappers().add(new CatalogXmlIRIMapper(catalogFile.getCanonicalFile()));
			} catch (IOException exception) {
				throw new CommitException("Could not load catalog file: "+catalogFile, exception, true);
			}
		}
		try {
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(scmFile);
			return ontology;
		} catch (OWLOntologyCreationException exception) {
			throw new CommitException("Could not load file: "+scmFile.getAbsolutePath(), exception, true);
		}
	}

}
