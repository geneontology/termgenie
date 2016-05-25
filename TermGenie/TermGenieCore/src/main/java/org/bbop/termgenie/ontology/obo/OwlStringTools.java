package org.bbop.termgenie.ontology.obo;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser;
import org.semanticweb.owlapi.functional.renderer.OWLFunctionalSyntaxRenderer;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import owltools.graph.OWLGraphWrapper;
import owltools.io.OWLPrettyPrinter;


public class OwlStringTools {
	
	static final OWLOntologyManager translationManager = OWLManager.createOWLOntologyManager();
	static final OWLOntologyLoaderConfiguration loaderConfiguration = translationManager.getOntologyLoaderConfiguration();
	
	public static String translateAxiomsToString(Set<OWLAxiom> axioms) {
		try {
			OWLOntology ontology = translationManager.createOntology();
			translationManager.addAxioms(ontology, axioms);
			OWLFunctionalSyntaxRenderer r = new OWLFunctionalSyntaxRenderer();
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
		if (axioms == null || axioms.isEmpty()) {
			return Collections.emptySet();
		}
		try {
			OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
			OWLOntologyDocumentSource documentSource = new StringDocumentSource(axioms);
			OWLOntology ontology = translationManager.createOntology();
			OWLOntologyLoaderConfiguration loaderConfiguration = ontology.getOWLOntologyManager().getOntologyLoaderConfiguration();
			p.parse(documentSource, ontology, loaderConfiguration);
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

	public static Set<OWLAxiom> replace(Set<OWLAxiom> axioms, Map<IRI, IRI> replacements, Map<String, String> idMappings) {
		OWLDataFactory factory = translationManager.getOWLDataFactory();
		OWLObjectDuplicator duplicator = new OWLObjectDuplicator(factory, replacements);
		Set<OWLAxiom> replaced = new HashSet<OWLAxiom>();
		IRI idIRI = IRI.create(Obo2OWLConstants.OIOVOCAB_IRI_PREFIX+OboFormatTag.TAG_ID.getTag());
		OWLAnnotationProperty idProperty = factory.getOWLAnnotationProperty(idIRI);
		for(OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLAnnotationAssertionAxiom) {
				boolean addAxiom = true;
				OWLAnnotationAssertionAxiom assertionAxiom = (OWLAnnotationAssertionAxiom) axiom;
				if (idProperty.equals(assertionAxiom.getProperty())) {
					OWLAnnotationValue value = assertionAxiom.getValue();
					if (value instanceof OWLLiteral) {
						String literal = ((OWLLiteral) value).getLiteral();
						if (idMappings.containsKey(literal)) {
							String permanentId = idMappings.get(literal);
							OWLAnnotationSubject subject = assertionAxiom.getSubject();
							if (subject instanceof IRI) {
								IRI subjectIRI = (IRI) subject;
								IRI replacedSubject = replacements.get(subjectIRI);
								if (replacedSubject != null) {
									subject = replacedSubject;
								}
							}
							replaced.add(factory.getOWLAnnotationAssertionAxiom(subject, factory.getOWLAnnotation(idProperty, factory.getOWLLiteral(permanentId))));
							addAxiom = false;
						}
					}
				}
				if (addAxiom) {
					replaced.add(duplicator.<OWLAxiom>duplicateObject(axiom));
				}
			}
			else {
				replaced.add(duplicator.<OWLAxiom>duplicateObject(axiom));
			}
		}
		return replaced;
	}
	
	public static String renderPretty(Set<OWLAxiom> axioms, OWLGraphWrapper graph) {
		// OWLPrettyPrinter pp = new OWLPrettyPrinter(graph);
		OWLPrettyPrinter pp = OWLPrettyPrinter.createManchesterSyntaxPrettyPrinter(graph);
		StringBuilder sb = new StringBuilder();
		for (OWLAxiom axiom : axioms) {
			sb.append(pp.render(axiom)).append("\n");
		}
		return sb.toString();
	}
}
