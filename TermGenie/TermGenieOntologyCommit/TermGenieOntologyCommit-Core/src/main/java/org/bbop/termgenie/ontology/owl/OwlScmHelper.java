package org.bbop.termgenie.ontology.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
		super(svnOntologyFileName);
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
	protected OWLOntology loadOntology(File scmFile) throws CommitException {
		// use a new manager to avoid 'already loaded' exceptions
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		if(iriMapper != null) {
			manager.addIRIMapper(iriMapper);
		}
		IRI iri = IRI.create(scmFile);
		try {
			OWLOntology ontology = manager.loadOntology(iri);
			return ontology;
		} catch (OWLOntologyCreationException exception) {
			throw new CommitException("Could not load file: "+scmFile.getAbsolutePath(), exception, true);
		}
	}

}
