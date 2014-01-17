package org.bbop.termgenie.ontology.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.IRIMapper;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owltools.graph.OWLGraphWrapper;

public abstract class OwlScmHelper extends ScmHelper<OWLOntology> {

	private final IRIMapper iriMapper;

	protected OwlScmHelper(IRIMapper iriMapper, String svnOntologyFileName)
	{
		super(svnOntologyFileName, null);
		this.iriMapper = iriMapper;
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
	public void createModifiedTargetFiles(ScmCommitData data,
			List<OWLOntology> ontologies,
			OWLGraphWrapper graph,
			String savedBy) throws CommitException
	{
		int ontologyCount = ontologies.size();
		List<File> modifiedTargetFiles = data.getModifiedTargetFiles();
		for (int i = 0; i < ontologyCount; i++) {
			// write changed ontology to a file
			final OWLOntology ontology = ontologies.get(i);
			createOwlFile(modifiedTargetFiles.get(i), ontology);
		}
	}
	
	private void createOwlFile(File owlFile, OWLOntology ontology) throws CommitException {
		OutputStream outputStream = null;
		try {
			// check that all folders are created
			owlFile.getParentFile().mkdirs();
			
			// write OWL file
			outputStream = new FileOutputStream(owlFile);
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
	protected List<OWLOntology> loadOntologies(List<File> scmFiles) throws CommitException {
		// use a new manager to avoid 'already loaded' exceptions
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		if(iriMapper != null) {
			manager.addIRIMapper(iriMapper);
		}
		List<OWLOntology> result = new ArrayList<OWLOntology>(scmFiles.size());
		for (File file : scmFiles) {
			IRI iri = IRI.create(file);
			try {
				OWLOntology ontology = manager.loadOntology(iri);
				result.add(ontology);
			} catch (OWLOntologyCreationException exception) {
				throw new CommitException("Could not load file: "+file.getAbsolutePath(), exception, true);
			}
		}
		return result;
	}

}
