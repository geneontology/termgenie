package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.Relation;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import owltools.graph.OWLGraphWrapper;

public class OwlTranslatorTools {

	public static List<IRelation> extractRelations(OWLClass owlClass,
			OWLGraphWrapper wrapper)
	{
		OWLOntology ontology = wrapper.getSourceOntology();
		String source = Owl2Obo.getIdentifier(owlClass.getIRI());
		List<IRelation> result = new ArrayList<IRelation>();

		// IS_A
		Set<OWLSubClassOfAxiom> subClassAxioms = ontology.getSubClassAxiomsForSubClass(owlClass);
		if (subClassAxioms != null && !subClassAxioms.isEmpty()) {
			for (OWLSubClassOfAxiom axiom : subClassAxioms) {
				OWLClassExpression sup = axiom.getSuperClass();
				if (sup instanceof OWLClass) {
					String target = Owl2Obo.getIdentifierFromObject(sup, ontology);
					String targetLabel = wrapper.getLabel(sup);
					Map<String, String> properties = new HashMap<String, String>(3);
					Relation.setType(properties, OboFormatTag.TAG_IS_A);
					result.add(new Relation(source, target, targetLabel, properties));
				}
				else if (sup instanceof OWLObjectSomeValuesFrom || sup instanceof OWLObjectAllValuesFrom) {
					OWLQuantifiedRestriction<?,?,?> r = (OWLQuantifiedRestriction<?,?,?>) sup;
					String target = Owl2Obo.getIdentifierFromObject(r.getFiller(), ontology);

					if (target != null) {
						String targetLabel = wrapper.getLabel(r.getFiller());
						Map<String, String> properties = new HashMap<String, String>(3);
						String reltype = Owl2Obo.getIdentifierFromObject(r.getProperty(), ontology);
						Relation.setType(properties, OboFormatTag.TAG_RELATIONSHIP, reltype);
						result.add(new Relation(source, target, targetLabel, properties));
					}
				}
			}
		}

		Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = ontology.getEquivalentClassesAxioms(owlClass);
		if (equivalentClassesAxioms != null && !equivalentClassesAxioms.isEmpty()) {
			for (OWLEquivalentClassesAxiom axiom : equivalentClassesAxioms) {
				List<OWLClassExpression> list = axiom.getClassExpressionsAsList();

				OWLClassExpression ce1 = list.get(0);
				OWLClassExpression ce2 = list.get(1);

				final String cls2 = Owl2Obo.getIdentifierFromObject(ce2, ontology);

				final String label = wrapper.getLabel(ce1);
				if (label == null) {
					continue;
				}

				boolean isUntranslateable = false;
				List<IRelation> equivalenceAxiomRelations = new ArrayList<IRelation>();

				if (cls2 != null) {
					// TODO OboFormatTag.TAG_EQUIVALENT_TO
				}
				else if (ce2 instanceof OWLObjectUnionOf) {
					List<OWLClassExpression> operands = ((OWLObjectUnionOf) ce2).getOperandsAsList();
					for (OWLClassExpression operand : operands) {
						String id = Owl2Obo.getIdentifierFromObject(operand, ontology);
						if (id == null) {
							isUntranslateable = true;
						}
						else {
							Map<String, String> properties = new HashMap<String, String>(3);
							Relation.setType(properties, OboFormatTag.TAG_UNION_OF);
							String targetLabel = wrapper.getLabel(operand);
							Relation rel = new Relation(source, id, targetLabel, properties);
							equivalenceAxiomRelations.add(rel);
						}
					}
				}
				else if (ce2 instanceof OWLObjectIntersectionOf) {

					List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) ce2).getOperandsAsList();
					for (OWLClassExpression operand : operands) {
						String r = null;
						String target = Owl2Obo.getIdentifierFromObject(operand, ontology);
						String targetLabel = wrapper.getLabel(operand);

						if (operand instanceof OWLObjectSomeValuesFrom) {
							OWLObjectSomeValuesFrom ristriction = (OWLObjectSomeValuesFrom) operand;
							r = Owl2Obo.getIdentifierFromObject(ristriction.getProperty(), ontology);
							target = Owl2Obo.getIdentifierFromObject(ristriction.getFiller(),
									ontology);
							targetLabel = wrapper.getLabel(ristriction.getFiller());
						}
						if (target != null) {
							Map<String, String> properties = new HashMap<String, String>(3);

							if (r == null) {
								Relation.setType(properties, OboFormatTag.TAG_INTERSECTION_OF);
							}
							else {
								Relation.setType(properties, OboFormatTag.TAG_INTERSECTION_OF, r);
							}
							Relation rel = new Relation(source, target, targetLabel, properties);
							equivalenceAxiomRelations.add(rel);
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
			for (OWLDisjointClassesAxiom axiom : disjointClassesAxioms) {
				Set<OWLClassExpression> expressions = axiom.getClassExpressionsMinus(owlClass);
				for (OWLClassExpression expression : expressions) {
					if (expression instanceof OWLClass) {
						OWLClass targetClass = (OWLClass) expression;
						String target = Owl2Obo.getIdentifierFromObject(targetClass, ontology);
						if (target != null) {
							String targetLabel = wrapper.getLabel(targetClass);
							Map<String, String> properties = new HashMap<String, String>(3);
							Relation.setType(properties, OboFormatTag.TAG_DISJOINT_FROM);
							result.add(new Relation(source, target, targetLabel, properties));
						}
					}	
				}
			}
		}
		return result;
	}
}
