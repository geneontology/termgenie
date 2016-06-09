package org.bbop.termgenie.services;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

public class NoopPreSubmitFilter implements PreSubmitFilter {

	@Override
	public Frame filterFrame(Frame frame) {
		return frame;
	}

	@Override
	public Set<OWLAxiom> filterAxioms(Set<OWLAxiom> axioms) {
		return axioms;
	}

	@Override
	public List<Pair<Frame, Set<OWLAxiom>>> filterChanged(List<Pair<Frame, Set<OWLAxiom>>> changed) {
		return changed;
	}
}
