package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.QualifierValue;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import owltools.graph.OWLGraphWrapper;

public class OwlTranslatorTools {

	public static Pair<List<Clause>, Set<OWLAxiom>> extractRelations(OWLClass owlClass, OWLGraphWrapper wrapper) {
		OWLOntology ontology = wrapper.getSourceOntology();
		List<Clause> result = new ArrayList<Clause>();
		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		handleSubClass(owlClass, ontology, result, relevantAxioms);

		handleEquivalenClasses(owlClass, ontology, result, relevantAxioms);

		handleDisjoints(owlClass, ontology, result, relevantAxioms);
		return new Pair<List<Clause>, Set<OWLAxiom>>(result, relevantAxioms);
	}

	private static void handleSubClass(OWLClass owlClass,
			OWLOntology ontology,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
		Set<OWLSubClassOfAxiom> subClassAxioms = ontology.getSubClassAxiomsForSubClass(owlClass);
		if (subClassAxioms == null || subClassAxioms.isEmpty()) {
			return;
		}
		
		relevantAxioms.addAll(subClassAxioms);
		for (OWLSubClassOfAxiom axiom : subClassAxioms) {
			OWLClassExpression sup = axiom.getSuperClass();
			if (sup instanceof OWLClass) {
				String target = getId(sup, ontology);
				Clause clause = new Clause(OboFormatTag.TAG_IS_A, target);
				addQualifiers(clause, axiom.getAnnotations());
				result.add(clause);
			}
			else if (sup instanceof OWLQuantifiedObjectRestriction) {
				// OWLObjectSomeValuesFrom
				// OWLObjectAllValuesFrom
				OWLQuantifiedObjectRestriction r = (OWLQuantifiedObjectRestriction) sup;
				String fillerId = getId(r.getFiller(), ontology);

				if(fillerId != null){
					result.add(createRelationshipClauseWithRestrictions(r, fillerId, axiom, ontology));
				}
			} else if (sup instanceof OWLObjectCardinalityRestriction) {
				// OWLObjectExactCardinality
				// OWLObjectMinCardinality
				// OWLObjectMaxCardinality
				OWLObjectCardinalityRestriction cardinality = (OWLObjectCardinalityRestriction) sup;
				String fillerId = getId(cardinality.getFiller(), ontology);
				if(fillerId != null){
					result.add(createRelationshipClauseWithCardinality(cardinality, fillerId, axiom, ontology));
				}
			} else if (sup instanceof OWLObjectIntersectionOf) {
				OWLObjectIntersectionOf i = (OWLObjectIntersectionOf) sup;
				List<Clause> clauses = new ArrayList<Clause>();
				for(OWLClassExpression operand : i.getOperands()) {
					if (operand instanceof OWLQuantifiedObjectRestriction) {
						OWLQuantifiedObjectRestriction restriction = (OWLQuantifiedObjectRestriction) operand;
						String fillerId = getId(restriction.getFiller(), ontology);
						if(fillerId == null){
							clauses.add(createRelationshipClauseWithRestrictions(restriction, fillerId, axiom, ontology));
						}
					}
					else if (operand instanceof OWLObjectCardinalityRestriction) {
						OWLObjectCardinalityRestriction restriction = (OWLObjectCardinalityRestriction) operand;
						String fillerId = getId(restriction.getFiller(), ontology);
						if(fillerId == null){
							clauses.add(createRelationshipClauseWithCardinality(restriction, fillerId, axiom, ontology));
						}
					}
				}
				clauses = Owl2Obo.normalizeRelationshipClauses(clauses);

				for (Clause clause : clauses) {
					result.add(clause);	
				}
			}
		}
	}
	
	private static Clause createRelationshipClauseWithRestrictions(OWLQuantifiedObjectRestriction r,
			String fillerId,
			OWLSubClassOfAxiom ax,
			OWLOntology ontology)
	{
		Clause c = new Clause();
		c.setTag(OboFormatTag.TAG_RELATIONSHIP.getTag());
		c.addValue(getId(r.getProperty(), ontology));
		c.addValue(fillerId);
		addQualifiers(c, ax.getAnnotations());
		return c;
	}

	private static Clause createRelationshipClauseWithCardinality(OWLObjectCardinalityRestriction restriction,
			String fillerId,
			OWLSubClassOfAxiom ax,
			OWLOntology ontology)
	{
		Clause c = new Clause();
		c.setTag(OboFormatTag.TAG_RELATIONSHIP.getTag());
		c.addValue(getId(restriction.getProperty(), ontology));
		c.addValue(fillerId);
		String q = "cardinality";
		if (restriction instanceof OWLObjectMinCardinality) {
			q = "minCardinality";
		}
		else if (restriction instanceof OWLObjectMaxCardinality) {
			q = "maxCardinality";
		}
		c.addQualifierValue(new QualifierValue(q, Integer.toString(restriction.getCardinality())));
		addQualifiers(c, ax.getAnnotations());
		return c;
	}

	private static void handleEquivalenClasses(OWLClass owlClass,
			OWLOntology ontology,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
		Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = ontology.getEquivalentClassesAxioms(owlClass);
		if (equivalentClassesAxioms != null && !equivalentClassesAxioms.isEmpty()) {
			relevantAxioms.addAll(equivalentClassesAxioms);
			for (OWLEquivalentClassesAxiom axiom : equivalentClassesAxioms) {
				handleEquivalenClassesAxiom(owlClass, axiom, ontology, result);
			}
		}
	}

	private static void handleEquivalenClassesAxiom(OWLClass owlClass,
			OWLEquivalentClassesAxiom axiom,
			OWLOntology ontology,
			List<Clause> result)
	{
		Set<OWLClassExpression> expressions = axiom.getClassExpressionsMinus(owlClass);
		if (expressions.size() != 1) {
			return;
		}

		Iterator<OWLClassExpression> it = expressions.iterator();
		OWLClassExpression ce2 = it.next();
		
		boolean isUntranslateable = false;
		List<Clause> equivalenceAxiomClauses = new ArrayList<Clause>();

		String cls2 = getId(ce2, ontology);
		if (cls2 != null) {
			Clause c = new Clause();
			c.setTag(OboFormatTag.TAG_EQUIVALENT_TO.getTag());
			c.setValue(cls2);
			result.add(c);
			addQualifiers(c, axiom.getAnnotations());
		} else if (ce2 instanceof OWLObjectUnionOf) {
			
			Set<OWLClassExpression> operands = ((OWLObjectUnionOf) ce2).getOperands();
			for(OWLClassExpression oce : operands){
				String id = getId(oce, ontology);
				Clause c = new Clause();
				c.setTag(OboFormatTag.TAG_UNION_OF.getTag());

				if(id != null){
					c.setValue(id);
					equivalenceAxiomClauses.add(c);
				}
				else {
					isUntranslateable = true;
					break;
				}
			}
		} else if (ce2 instanceof OWLObjectIntersectionOf) {

			Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) ce2).getOperands();
			for(OWLClassExpression ce : operands){
				String r = null;
				cls2 = getId(ce, ontology);

				if(ce instanceof OWLObjectSomeValuesFrom ){
					OWLObjectSomeValuesFrom ristriction = (OWLObjectSomeValuesFrom)ce;
					r = getId(ristriction.getProperty(), ontology);
					cls2 = getId(ristriction.getFiller(), ontology);
				}

				if(cls2 != null){
					Clause c = new Clause();
					c.setTag(OboFormatTag.TAG_INTERSECTION_OF.getTag());

					if(r != null) {
						c.addValue(r);
					}

					c.addValue(cls2);
					equivalenceAxiomClauses.add(c);
				}
				else if (hasClause(OboFormatTag.TAG_INTERSECTION_OF, result)) {
					isUntranslateable = true;
					break;
				}
				else {
					isUntranslateable = true;
					break;
				}
			}
		}

		// Only add clauses if the *entire* equivalence axiom can be translated
		if (!isUntranslateable) {
			for (Clause c : equivalenceAxiomClauses) {
				result.add(c);
			}
		}
	}
	
	private static boolean hasClause(OboFormatTag tag, Collection<Clause> clauses) {
		String tagString = tag.getTag();
		for (Clause clause : clauses) {
			if (tagString.equals(clause.getTag())) {
				return true;
			}
		}
		return false;
	}
	
	private static void handleDisjoints(OWLClass owlClass,
			OWLOntology ontology,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
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
							Clause c = new Clause(OboFormatTag.TAG_DISJOINT_FROM, target);
							addQualifiers(c, axiom.getAnnotations());
							result.add(c);
						}
					}
				}
			}
		}
	}

	private static void addQualifiers(Clause c, Set<OWLAnnotation> qualifiers){
		if (qualifiers == null || qualifiers.isEmpty()) {
			return;
		}
		for (OWLAnnotation ann : qualifiers){
			String prop = Owl2Obo.owlObjectToTag(ann.getProperty());

			if (prop == null) {
				prop = ann.getProperty().getIRI().toString();
			}

			if (prop.equals("gci_relation") ||
					prop.equals("gci_filler") ||
					prop.equals("cardinality") ||
					prop.equals("minCardinality") ||
					prop.equals("maxCardinality")) {
				continue;
			}

			String value = ann.getValue().toString();

			if(ann.getValue() instanceof OWLLiteral){
				value = ((OWLLiteral) ann.getValue()).getLiteral();
			}else if(ann.getValue() instanceof IRI){
				value = Owl2Obo.getIdentifier((IRI)ann.getValue());
			}

			QualifierValue qv = new QualifierValue(prop, value);
			c.addQualifierValue(qv);

		}
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
