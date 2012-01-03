package org.bbop.termgenie.rules;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Static helper methods for the term generation.
 */
public class RuleHelper {
	
	/**
	 * Check if the target class has equivalence axioms, which has any of checkedFor classes in its signature.
	 * 
	 * @param targetClass
	 * @param checkedForClasses
	 * @param ontology
	 * @return true, if there is an equivalence axiom which has any of checkedFor classes in its signature
	 */
	public static boolean containsClassInEquivalenceAxioms(OWLClass targetClass, Set<OWLClass> checkedForClasses, OWLOntology ontology) {
		if (targetClass == null || checkedForClasses == null || checkedForClasses.isEmpty()) {
			return false;
		}
		Set<OWLEquivalentClassesAxiom> axioms = ontology.getEquivalentClassesAxioms(targetClass);
		if (!axioms.isEmpty()) {
			for (OWLEquivalentClassesAxiom axiom : axioms) {
				Set<OWLClass> classesInSignature = axiom.getClassesInSignature();
				if (!classesInSignature.isEmpty()) {
					for(OWLClass checkedFor : checkedForClasses) {
						boolean contains = classesInSignature.contains(checkedFor);
						if (contains) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
