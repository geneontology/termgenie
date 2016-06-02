package org.bbop.termgenie.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import owltools.graph.AxiomAnnotationTools;
import owltools.graph.OWLGraphWrapper;

import com.google.common.collect.Sets;


public class InferAllRelationshipsTask implements RelationshipTask {
	
	private final OWLOntology disposable;
	private OWLGraphWrapper reference;
	private final IRI iri;
	private final String tempIdPrefix;
	private final ProcessState state;
	private final boolean useIsInferred;
	
	private InferredRelations result;
	

	public InferAllRelationshipsTask(OWLOntology disposable, OWLGraphWrapper reference, IRI iri, String tempIdPrefix, ProcessState state, boolean useIsInferred) {
		super();
		this.disposable = disposable;
		this.reference = reference;
		this.iri = iri;
		this.tempIdPrefix = tempIdPrefix;
		this.state = state;
		this.useIsInferred = useIsInferred;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		
		// check for equivalent classes
		ProcessState.addMessage(state, "Check for equivalent classes of new term");
		OWLClass owlClass = disposable.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
		Set<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass).getEntitiesMinus(owlClass);
		if (equivalentClasses.isEmpty() == false) {
			result = new InferredRelations(equivalentClasses);
			return Modified.no;
		}
		
		// remove redundant links and assert inferred ones
		ProcessState.addMessage(state, "Check for changed relations");
		applyInferences(reasoner, owlClass);

		List<Pair<Frame, Set<OWLAxiom>>> changed = null;
		Set<OWLClass> subClasses = reasoner.getSubClasses(owlClass, true).getFlattened();
		if (subClasses != null && !subClasses.isEmpty()) {
			changed = new ArrayList<Pair<Frame, Set<OWLAxiom>>>();
			for (OWLClass subClass : subClasses) {
				if (subClass.isBottomEntity()) {
					// skip owl:Nothing
					continue;
				}
				applyInferences(reasoner, subClass);
				String subClassIRI = subClass.getIRI().toString();
				if (!subClassIRI.startsWith(tempIdPrefix)) {
					Frame frame = OboTools.createTermFrame(subClass);
					Pair<List<Clause>, Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(subClass,
							disposable, reference);
					List<Clause> clauses = pair.getOne();
					OBOFormatWriter.sortTermClauses(clauses);
					frame.getClauses().addAll(clauses);
					changed.add(Pair.of(frame, pair.getTwo()));

				}
			}
			if (changed.isEmpty()) {
				changed = null;
			}
			Pair<List<Clause>,Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(owlClass, disposable, reference);
			result = new InferredRelations(pair.getOne(), pair.getTwo(), changed);
		}
		return Modified.no;
	}

	protected void applyInferences(OWLReasoner reasoner, OWLClass cls) {
		Set<OWLAxiom> addAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> removeAxioms = new HashSet<OWLAxiom>();
		OWLOntologyManager manager = disposable.getOWLOntologyManager();
		handleInferences(cls, reasoner, disposable, addAxioms, removeAxioms, useIsInferred);
		manager.addAxioms(disposable, addAxioms);
		manager.removeAxioms(disposable, removeAxioms);
	}

	/**
	 * Clean up the 'relations' for the given class:
	 * <ul>
	 *  <li>Assert direct super classes</li>
	 *  <li>Remove assertions for non-direct super classes</li>
	 *  <li>weaken intersections restrictions into sub class axioms</li>
	 *  <li>Optional: add marked as inferred axioms</li>
	 *  <li>axioms are not added or removed directly from the ontology, but they are added to the respective sets</li>
	 * </ul>
	 * 
	 * @param cls
	 * @param reasoner
	 * @param ontology
	 * @param addAxioms
	 * @param removeAxiom
	 * @param useIsInferred
	 */
	public static void handleInferences(OWLClass cls, OWLReasoner reasoner, OWLOntology ontology, 
			final Set<OWLAxiom> addAxioms, final Set<OWLAxiom> removeAxiom, final boolean useIsInferred) 
	{
		final OWLOntologyManager manager = ontology.getOWLOntologyManager();
		final OWLDataFactory dataFactory = manager.getOWLDataFactory();
		final OWLClass owlThing = dataFactory.getOWLThing();
		
		// get all direct super classes
		final Set<OWLClass> direct = reasoner.getSuperClasses(cls, true).getFlattened();
		direct.remove(owlThing);

		// get all super classes (includes direct ones)
		final Set<OWLClass> indirect = reasoner.getSuperClasses(cls, false).getFlattened();
		indirect.remove(owlThing);

		
		final Map<OWLClassExpression, OWLAxiom> assertedSuperClasses = new HashMap<OWLClassExpression, OWLAxiom>();
		// handle equiv class statements: weaken intersections into subClass statements
		handleIntersections(cls, ontology, addAxioms, assertedSuperClasses, useIsInferred);
		
		// get asserted super classes
		final Set<OWLSubClassOfAxiom> assertedAxioms = ontology.getSubClassAxiomsForSubClass(cls);
		for (final OWLSubClassOfAxiom ax : assertedAxioms) {
			OWLClassExpression superClsExp = ax.getSuperClass();
			assertedSuperClasses.put(superClsExp, ax);
		}
		
		// assert direct super classes
		Set<OWLClass> nonAssertedDirectSupers = Sets.difference(direct, assertedSuperClasses.keySet());
		for (OWLClass superCls : nonAssertedDirectSupers) {
			OWLAxiom newAxiom = dataFactory.getOWLSubClassOfAxiom(cls, superCls);
			if (useIsInferred) {
				newAxiom = AxiomAnnotationTools.markAsInferredAxiom(newAxiom, dataFactory);
			}
			addAxioms.add(newAxiom);
		}
		
		// remove asserted non-direct super classes
		Set<OWLClass> nonDirectSuperClasses = Sets.difference(indirect, direct);
		for (OWLClass nonDirectSuperCls : nonDirectSuperClasses) {
			OWLAxiom ax = assertedSuperClasses.get(nonDirectSuperCls);
			if (ax != null) {
				removeAxiom.add(ax);
			}
		}
	}
	
	private static void handleIntersections(final OWLClass cls, OWLOntology ontology, 
			final Set<OWLAxiom> addAxioms, final Map<OWLClassExpression, OWLAxiom> assertedSuperClasses,
			final boolean useIsInferred)
	{
		final OWLOntologyManager manager = ontology.getOWLOntologyManager();
		final OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		Set<OWLEquivalentClassesAxiom> axioms = ontology.getEquivalentClassesAxioms(cls);
		for (OWLEquivalentClassesAxiom ax : axioms) {
			for(OWLClassExpression expr : ax.getClassExpressions()) {
				// only look for intersections
				expr.accept(new OWLClassExpressionVisitorAdapter(){

					@Override
					public void visit(OWLObjectIntersectionOf intersection) {
						for(OWLClassExpression operand : intersection.getOperands()) {
							// only look at restrictions, simple subClassOf is handled by the reasoner
							if (operand instanceof OWLRestriction) {
								OWLAxiom subClassOfAx = dataFactory.getOWLSubClassOfAxiom(cls, operand);
								if (useIsInferred) {
									subClassOfAx = AxiomAnnotationTools.markAsInferredAxiom(subClassOfAx, dataFactory);
								}
								addAxioms.add(subClassOfAx);
								assertedSuperClasses.put(operand, subClassOfAx);
							}
						}
					}
				});
			}
		}
	}
	
	/**
	 * @return the {@link InferredRelations}
	 */
	@Override
	public final InferredRelations getInferredRelations() {
		return result;
	}

}
