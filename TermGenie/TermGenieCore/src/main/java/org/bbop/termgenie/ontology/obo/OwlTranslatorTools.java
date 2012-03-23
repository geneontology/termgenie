package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import owltools.graph.OWLGraphWrapper;

public class OwlTranslatorTools {

	public static Pair<List<Clause>, Set<OWLAxiom>> extractRelations(OWLClass owlClass, OWLGraphWrapper wrapper) {
		OWLOntology ontology = wrapper.getSourceOntology();
		List<Clause> result = new ArrayList<Clause>();
		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		// IS_A
		Set<OWLSubClassOfAxiom> subClassAxioms = ontology.getSubClassAxiomsForSubClass(owlClass);
		if (subClassAxioms != null && !subClassAxioms.isEmpty()) {
			relevantAxioms.addAll(subClassAxioms);
			for (OWLSubClassOfAxiom axiom : subClassAxioms) {
				OWLClassExpression sup = axiom.getSuperClass();
				if (sup instanceof OWLClass) {
					String target = getId(sup, ontology);
					result.add(new Clause(OboFormatTag.TAG_IS_A, target));
				}
				else if (sup instanceof OWLObjectSomeValuesFrom || sup instanceof OWLObjectAllValuesFrom) {
					OWLQuantifiedRestriction<?, ?, ?> r = (OWLQuantifiedRestriction<?, ?, ?>) sup;
					String target = getId(r.getFiller(), ontology);

					if (target != null) {
						String reltype = getId(r.getProperty(), ontology);
						Clause cl = new Clause(OboFormatTag.TAG_RELATIONSHIP);
						cl.addValue(reltype);
						cl.addValue(target);
						result.add(cl);
					}
				}
			}
		}

		Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = ontology.getEquivalentClassesAxioms(owlClass);
		if (equivalentClassesAxioms != null && !equivalentClassesAxioms.isEmpty()) {
			relevantAxioms.addAll(equivalentClassesAxioms);
			for (OWLEquivalentClassesAxiom axiom : equivalentClassesAxioms) {
				List<OWLClassExpression> list = axiom.getClassExpressionsAsList();

				OWLClassExpression ce1 = list.get(0);
				OWLClassExpression ce2 = list.get(1);

				boolean isUntranslateable = false;

				final String cls2 = getId(ce2, ontology);

				final String label = wrapper.getLabel(ce1);
				if (label == null) {
					continue;
				}

				List<Clause> equivalenceAxiomRelations = new ArrayList<Clause>();

				if (cls2 != null) {
					// TODO OboFormatTag.TAG_EQUIVALENT_TO
				}
				else if (ce2 instanceof OWLObjectUnionOf) {
					List<OWLClassExpression> operands = ((OWLObjectUnionOf) ce2).getOperandsAsList();
					for (OWLClassExpression operand : operands) {
						String id = getId(operand, ontology);
						if (id == null) {
							isUntranslateable = true;
						}
						else {
							equivalenceAxiomRelations.add(new Clause(OboFormatTag.TAG_UNION_OF, id));
						}
					}
				}
				else if (ce2 instanceof OWLObjectIntersectionOf) {

					List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) ce2).getOperandsAsList();
					for (OWLClassExpression operand : operands) {
						String r = null;
						String target = getId(operand, ontology);

						if (operand instanceof OWLObjectSomeValuesFrom) {
							OWLObjectSomeValuesFrom ristriction = (OWLObjectSomeValuesFrom) operand;
							r = getId(ristriction.getProperty(), ontology);
							target = getId(ristriction.getFiller(), ontology);
						}
						if (target != null) {
							Clause cl = new Clause(OboFormatTag.TAG_INTERSECTION_OF);
							if (r != null) {
								cl.addValue(r);
							}
							cl.addValue(target);
							equivalenceAxiomRelations.add(cl);
						}
						else {
							isUntranslateable = true;
						}
					}
				}
				// Only add clauses if the *entire* equivalence axiom can be
				// translated
				if (!isUntranslateable) {
					result.addAll(equivalenceAxiomRelations);
				}
			}
		}

		// DISJOINT_FROM
		Set<OWLDisjointClassesAxiom> disjointClassesAxioms = ontology.getDisjointClassesAxioms(owlClass);
		if (disjointClassesAxioms != null && !disjointClassesAxioms.isEmpty()) {
			relevantAxioms.addAll(disjointClassesAxioms);
			for (OWLDisjointClassesAxiom axiom : disjointClassesAxioms) {
				Set<OWLClassExpression> expressions = axiom.getClassExpressionsMinus(owlClass);
				for (OWLClassExpression expression : expressions) {
					if (expression instanceof OWLClass) {
						OWLClass targetClass = (OWLClass) expression;
						String target = getId(targetClass, ontology);
						if (target != null) {
							result.add(new Clause(OboFormatTag.TAG_DISJOINT_FROM, target));
						}
					}
				}
			}
		}
		return new Pair<List<Clause>, Set<OWLAxiom>>(result, relevantAxioms);
	}

	private static String getId(OWLObject obj, OWLOntology ontology) {
		return Owl2Obo.getIdentifierFromObject(obj, ontology, null);
	}
	
	public static OWLAxiom createLabelAxiom(String id, String label, OWLGraphWrapper graph) {
		final OWLDataFactory factory = graph.getManager().getOWLDataFactory();
		return factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(),
				IRI.create(id), 
				factory.getOWLLiteral(label));
	}
	
}
