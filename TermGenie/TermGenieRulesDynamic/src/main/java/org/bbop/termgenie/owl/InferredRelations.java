package org.bbop.termgenie.owl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public class InferredRelations {

	public static final InferredRelations EMPTY = new InferredRelations(Collections.<Clause> emptyList(), Collections.<OWLAxiom> emptySet(), null);

	List<Clause> classRelations = null;
	Set<OWLAxiom> classRelationAxioms = null;
	List<Pair<Frame, Set<OWLAxiom>>> changed = null;
	Set<OWLClass> equivalentClasses = null;

	/**
	 * @param equivalentClasses
	 */
	InferredRelations(Set<OWLClass> equivalentClasses) {
		this.equivalentClasses = equivalentClasses;
	}

	/**
	 * @param classRelations
	 * @param classRelationAxioms
	 * @param changed
	 */
	InferredRelations(List<Clause> classRelations,
			Set<OWLAxiom> classRelationAxioms,
			List<Pair<Frame, Set<OWLAxiom>>> changed)
	{
		this.classRelations = classRelations;
		this.classRelationAxioms = classRelationAxioms;
		this.changed = changed;
	}

	/**
	 * @return the classRelations
	 */
	public List<Clause> getClassRelations() {
		return classRelations;
	}

	/**
	 * @return the classRelationAxioms
	 */
	public Set<OWLAxiom> getClassRelationAxioms() {
		return classRelationAxioms;
	}

	/**
	 * @param classRelationAxioms the classRelationAxioms to set
	 */
	public void setClassRelationAxioms(Set<OWLAxiom> classRelationAxioms) {
		this.classRelationAxioms = classRelationAxioms;
	}

	/**
	 * @return the changed
	 */
	public List<Pair<Frame, Set<OWLAxiom>>> getChanged() {
		return changed;
	}

	/**
	 * @return the equivalentClasses
	 */
	public Set<OWLClass> getEquivalentClasses() {
		return equivalentClasses;
	}
}
