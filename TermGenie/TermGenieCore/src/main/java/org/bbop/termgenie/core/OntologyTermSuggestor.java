package org.bbop.termgenie.core;

import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;

import owltools.graph.OWLGraphWrapper.ISynonym;

public interface OntologyTermSuggestor {

	public List<OntologyTerm<ISynonym, IRelation>> suggestTerms(String query, Ontology ontology, int maxCount);
}
