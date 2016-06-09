package org.bbop.termgenie.services;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

public interface PreSubmitFilter {

	public Frame filterFrame(Frame frame);
	
	public Set<OWLAxiom> filterAxioms(Set<OWLAxiom> axioms);
	
	public List<Pair<Frame,Set<OWLAxiom>>> filterChanged(List<Pair<Frame,Set<OWLAxiom>>> changed);
}
