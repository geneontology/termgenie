package org.bbop.termgenie.ontology.obo;

import java.util.Collection;

import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;


public class OwlTools {

	private static final class DeprecatedFinder extends OWLAxiomVisitorAdapter {

		private final IRI deprecatedIRI;
		boolean hasDeprecatedAnnotation = false;

		private DeprecatedFinder(IRI deprecatedIRI) {
			this.deprecatedIRI = deprecatedIRI;
		}

		@Override
		public void visit(OWLAnnotationAssertionAxiom axiom) {
			OWLAnnotationProperty property = axiom.getProperty();
			if (deprecatedIRI.equals(property.getIRI())) {
				hasDeprecatedAnnotation = true;
			}
		}
	}

	public static boolean isObsolete(Collection<OWLAxiom> axioms) {
		final IRI deprecatedIRI = OWLRDFVocabulary.OWL_DEPRECATED.getIRI();
		final DeprecatedFinder finder = new DeprecatedFinder(deprecatedIRI);
		for (OWLAxiom axiom : axioms) {
			axiom.accept(finder);
			if (finder.hasDeprecatedAnnotation) {
				return true;
			}
		}
		return false;
	}
	
	public static void addObsoleteAxiom(Collection<OWLAxiom> axioms, IRI classIRI) {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLDataFactory f = m.getOWLDataFactory();
		OWLAnnotationProperty p = f.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_DEPRECATED.getIRI());
		OWLAnnotation annotation = f.getOWLAnnotation(p, f.getOWLLiteral(Boolean.TRUE));
		axioms.add(f.getOWLAnnotationAssertionAxiom(classIRI, annotation));
	}
	
	public static IRI translateFrameIdToClassIRI(String id) {
		Obo2Owl tr = new Obo2Owl();
		tr.setObodoc(new OBODoc());
		return tr.oboIdToIRI(id);
	}
}
