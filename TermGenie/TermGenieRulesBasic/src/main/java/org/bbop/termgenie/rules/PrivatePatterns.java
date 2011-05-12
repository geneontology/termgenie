package org.bbop.termgenie.rules;

import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class PrivatePatterns extends Patterns {

	/**
	 * @param ontology
	 */
	protected PrivatePatterns(OWLGraphWrapper ontology) {
		super(ontology);
	}
	
	protected OntologyTerm regulation(OWLObject x) {
		String id = createNewId();
		String label = "regulation of "+ name(x);
		String description = "Any process that modulates the frequency, rate or extent of "+name(x)+".";
		Set<String> synonyms = synonyms(x, "regulation of ", null);
		String logicalDefinition = "cdef('GO:0065007',[regulates="+id(x)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}
	
	protected OntologyTerm negative_regulation(OWLObject x) {
		String id = createNewId();
		String label = "negative regulation of "+ name(x);
		String description = "Any process that modulates the frequency, rate or extent of "+name(x)+".";
		Set<String> synonyms = synonyms(x, "negative regulation of ", null);
		String logicalDefinition = "cdef('GO:0065007',[negatively_regulates="+id(x)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}

	protected OntologyTerm positive_regulation(OWLObject x) {
		String id = createNewId();
		String label = "positive regulation of "+ name(x);
		String description = "Any process that activates or increases the frequency, rate or extent of "+name(x)+".";
		Set<String> synonyms = synonyms(x, "positive regulation of ", null);
		String logicalDefinition = "cdef('GO:0065007',[positively_regulates="+id(x)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}
}