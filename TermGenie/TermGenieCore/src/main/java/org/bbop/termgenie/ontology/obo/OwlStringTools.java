package org.bbop.termgenie.ontology.obo;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxOWLParser;
import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;


public class OwlStringTools {
	
	static final OWLOntologyManager translationManager = OWLManager.createOWLOntologyManager();
	
	public static String translateAxiomsToString(Set<OWLAxiom> axioms) {
		try {
			OWLOntology ontology = translationManager.createOntology();
			translationManager.addAxioms(ontology, axioms);
			OWLFunctionalSyntaxRenderer r = new OWLFunctionalSyntaxRenderer(translationManager);
			Writer writer = new StringWriter();
			r.render(ontology, writer);
			return writer.toString();
		} catch (OWLRendererException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static Set<OWLAxiom> translateStringToAxioms(String axioms) {
		try {
			OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
			OWLOntologyDocumentSource documentSource = new StringDocumentSource(axioms);
			OWLOntology ontology = translationManager.createOntology();
			p.parse(documentSource, ontology);
			return ontology.getAxioms();
		} catch (UnloadableImportException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (OWLParserException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public static Set<OWLAxiom> replace(Set<OWLAxiom> axioms, Map<IRI, IRI> replacements) {
		OWLObjectDuplicator duplicator = new OWLObjectDuplicator(translationManager.getOWLDataFactory(), replacements);
		Set<OWLAxiom> replaced = new HashSet<OWLAxiom>();
		for(OWLAxiom axiom : axioms) {
			replaced.add(duplicator.<OWLAxiom>duplicateObject(axiom));
		}
		return replaced;
	}
}
