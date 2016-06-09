package org.bbop.termgenie.servlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.services.PreSubmitFilter;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.QualifierValue;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.AxiomAnnotationTools;

public class NoInferencesPreSubmitFilter implements PreSubmitFilter {

	@Override
	public Frame filterFrame(Frame frame) {
		List<Clause> filtered = new ArrayList<>();
		for(Clause clause : frame.getClauses()) {
			Collection<QualifierValue> qualifierValues = clause.getQualifierValues();
			boolean remove = false;
			for(QualifierValue qualifierValue : qualifierValues) {
				String qualifier = qualifierValue.getQualifier();
				String value = qualifierValue.getValue();
				if ("is_inferred".equalsIgnoreCase(qualifier) && "true".equalsIgnoreCase(value)) {
					remove = true;
				}
			}
			if (remove == false) {
				filtered.add(clause);
			}
		}
		frame.setClauses(filtered);
		frame.freeze();
		return frame;
	}

	@Override
	public Set<OWLAxiom> filterAxioms(Set<OWLAxiom> axioms) {
		Set<OWLAxiom> filtered = new HashSet<>();
		for(OWLAxiom axiom : axioms) {
			boolean isInferred = AxiomAnnotationTools.isMarkedAsInferredAxiom(axiom);
			if (isInferred == false) {
				filtered.add(axiom);
			}
		}
		return filtered;
	}

	@Override
	public List<Pair<Frame, Set<OWLAxiom>>> filterChanged(List<Pair<Frame, Set<OWLAxiom>>> changed) {
		return null; // intentionally null!
	}
}
