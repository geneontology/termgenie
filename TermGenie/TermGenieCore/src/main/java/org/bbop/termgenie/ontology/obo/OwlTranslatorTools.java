package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.QualifierValue;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import owltools.graph.OWLGraphWrapper;

public class OwlTranslatorTools {

	static {
		java.util.logging.Logger.getLogger("org.obolibrary").setLevel(Level.SEVERE);
	}
	
	public static Pair<List<Clause>, Set<OWLAxiom>> extractRelations(OWLClass owlClass, OWLOntology ontology, OWLGraphWrapper wrapper) {
		List<Clause> result = new ArrayList<Clause>();
		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		handleSubClass(owlClass, ontology, wrapper, result, relevantAxioms);

		handleEquivalenClasses(owlClass, ontology, wrapper, result, relevantAxioms);

		handleDisjoints(owlClass, ontology, wrapper, result, relevantAxioms);
		return Pair.of(result, relevantAxioms);
	}

	private static void handleSubClass(OWLClass owlClass,
			OWLOntology source,
			OWLGraphWrapper wrapper,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
		Set<OWLSubClassOfAxiom> subClassAxioms = source.getSubClassAxiomsForSubClass(owlClass);
		if (subClassAxioms == null || subClassAxioms.isEmpty()) {
			return;
		}
		
		relevantAxioms.addAll(subClassAxioms);
		for (OWLSubClassOfAxiom axiom : subClassAxioms) {
			OWLClassExpression sup = axiom.getSuperClass();
			if (sup instanceof OWLClass) {
				String target = getId(sup, wrapper);
				Clause clause = new Clause(OboFormatTag.TAG_IS_A, target);
				addQualifiers(clause, axiom.getAnnotations());
				result.add(clause);
			}
			else if (sup instanceof OWLQuantifiedObjectRestriction) {
				// OWLObjectSomeValuesFrom
				// OWLObjectAllValuesFrom
				OWLQuantifiedObjectRestriction r = (OWLQuantifiedObjectRestriction) sup;
				String fillerId = getId(r.getFiller(), wrapper);

				if(fillerId != null){
					result.add(createRelationshipClauseWithRestrictions(r, fillerId, axiom, wrapper));
				}
			} else if (sup instanceof OWLObjectCardinalityRestriction) {
				// OWLObjectExactCardinality
				// OWLObjectMinCardinality
				// OWLObjectMaxCardinality
				OWLObjectCardinalityRestriction cardinality = (OWLObjectCardinalityRestriction) sup;
				String fillerId = getId(cardinality.getFiller(), wrapper);
				if(fillerId != null){
					result.add(createRelationshipClauseWithCardinality(cardinality, fillerId, axiom, wrapper));
				}
			} else if (sup instanceof OWLObjectIntersectionOf) {
				OWLObjectIntersectionOf i = (OWLObjectIntersectionOf) sup;
				List<Clause> clauses = new ArrayList<Clause>();
				for(OWLClassExpression operand : i.getOperands()) {
					if (operand instanceof OWLQuantifiedObjectRestriction) {
						OWLQuantifiedObjectRestriction restriction = (OWLQuantifiedObjectRestriction) operand;
						String fillerId = getId(restriction.getFiller(), wrapper);
						if(fillerId == null){
							clauses.add(createRelationshipClauseWithRestrictions(restriction, fillerId, axiom, wrapper));
						}
					}
					else if (operand instanceof OWLObjectCardinalityRestriction) {
						OWLObjectCardinalityRestriction restriction = (OWLObjectCardinalityRestriction) operand;
						String fillerId = getId(restriction.getFiller(), wrapper);
						if(fillerId == null){
							clauses.add(createRelationshipClauseWithCardinality(restriction, fillerId, axiom, wrapper));
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
			OWLGraphWrapper wrapper)
	{
		Clause c = new Clause(OboFormatTag.TAG_RELATIONSHIP.getTag());
		c.addValue(getId(r.getProperty(), wrapper));
		c.addValue(fillerId);
		addQualifiers(c, ax.getAnnotations());
		return c;
	}

	private static Clause createRelationshipClauseWithCardinality(OWLObjectCardinalityRestriction restriction,
			String fillerId,
			OWLSubClassOfAxiom ax,
			OWLGraphWrapper wrapper)
	{
		Clause c = new Clause(OboFormatTag.TAG_RELATIONSHIP.getTag());
		c.addValue(getId(restriction.getProperty(), wrapper));
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
			OWLOntology source,
			OWLGraphWrapper wrapper,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
		Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = source.getEquivalentClassesAxioms(owlClass);
		if (equivalentClassesAxioms != null && !equivalentClassesAxioms.isEmpty()) {
			relevantAxioms.addAll(equivalentClassesAxioms);
			for (OWLEquivalentClassesAxiom axiom : equivalentClassesAxioms) {
				handleEquivalenClassesAxiom(owlClass, axiom, wrapper, result);
			}
		}
	}

	private static void handleEquivalenClassesAxiom(OWLClass owlClass,
			OWLEquivalentClassesAxiom axiom,
			OWLGraphWrapper ontology,
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
			Clause c = new Clause(OboFormatTag.TAG_EQUIVALENT_TO.getTag());
			c.setValue(cls2);
			result.add(c);
			addQualifiers(c, axiom.getAnnotations());
		} else if (ce2 instanceof OWLObjectUnionOf) {
			
			Set<OWLClassExpression> operands = ((OWLObjectUnionOf) ce2).getOperands();
			for(OWLClassExpression oce : operands){
				String id = getId(oce, ontology);
				Clause c = new Clause(OboFormatTag.TAG_UNION_OF.getTag());

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
					Clause c = new Clause(OboFormatTag.TAG_INTERSECTION_OF.getTag());

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
			OWLOntology source,
			OWLGraphWrapper wrapper,
			List<Clause> result,
			Set<OWLAxiom> relevantAxioms)
	{
		// DISJOINT_FROM
		Set<OWLDisjointClassesAxiom> disjointClassesAxioms = source.getDisjointClassesAxioms(owlClass);
		if (disjointClassesAxioms != null && !disjointClassesAxioms.isEmpty()) {
			relevantAxioms.addAll(disjointClassesAxioms);
			for (OWLDisjointClassesAxiom axiom : disjointClassesAxioms) {
				Set<OWLClassExpression> expressions = axiom.getClassExpressionsMinus(owlClass);
				for (OWLClassExpression expression : expressions) {
					if (expression instanceof OWLClass) {
						OWLClass targetClass = (OWLClass) expression;
						String target = getId(targetClass, wrapper);
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
	
	private static String getId(OWLObject obj, OWLGraphWrapper graph) {
		return getId(obj, graph.getSourceOntology());
	}
	
	private static String getId(OWLObject obj, OWLOntology ontology) {
		return Owl2Obo.getIdentifierFromObject(obj, ontology, null);
	}
	
	public static Frame generateFrame(Set<OWLAxiom> axioms, String id) throws OWLOntologyCreationException {
		Owl2Obo owl2Obo = new Owl2Obo();
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology temp = m.createOntology(axioms);
		OBODoc oboDoc = owl2Obo.convert(temp);
		Frame frame = oboDoc.getTermFrame(id);
		return frame;
	}
	
	public static OWLAxiom createLabelAxiom(String id, String label, OWLGraphWrapper graph) {
		final OWLDataFactory factory = graph.getManager().getOWLDataFactory();
		return factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(),
				IRI.create(id), 
				factory.getOWLLiteral(label));
	}
	
	public static Set<OWLAxiom> generateAxioms(Frame frame, OWLOntology target) {
		final Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Obo2Owl tr = new Obo2OwlExtension(axioms, target);
		tr.trTermFrame(frame);
		return axioms;
	}
	
	public static Set<OWLAxiom> translate(Collection<Clause> clauses, OWLClass cls, OWLOntology target) {
		final Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Obo2Owl tr = new Obo2OwlExtension(axioms, target);
		Map<String, List<Clause>> groupedClauses = new HashMap<String, List<Clause>>();
		for (Clause cl : clauses) {
			String t = cl.getTag();
			List<Clause> group = groupedClauses.get(t);
			if (group == null) {
				group = new ArrayList<Clause>();
				groupedClauses.put(t, group);
			}
			group.add(cl);
		}
		for(Entry<String, List<Clause>> entry : groupedClauses.entrySet()) {
			axioms.addAll(tr.trTermFrameClauses(cls, entry.getValue(), entry.getKey()));	
		}
		return axioms;
	}


	private static final class Obo2OwlExtension extends Obo2Owl {
	
		private final Set<OWLAxiom> axioms;
	
		private Obo2OwlExtension(Set<OWLAxiom> axioms, OWLOntology target) {
			super(target.getOWLOntologyManager());
			addDeclaredAnnotationProperties(target.getAnnotationPropertiesInSignature());
			this.axioms = axioms;
		}
	
		@Override
		protected void add(Set<OWLAxiom> toAdd) {
			axioms.addAll(toAdd);
		}
	}
}
