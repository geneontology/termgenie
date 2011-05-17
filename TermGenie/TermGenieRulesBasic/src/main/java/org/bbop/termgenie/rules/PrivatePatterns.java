package org.bbop.termgenie.rules;

import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

abstract class PrivatePatterns extends Patterns {

	protected OntologyTerm regulation(OWLObject x, OWLGraphWrapper ontology) {
		String id = createNewId();
		String label = "regulation of "+ name(x, ontology);
		String description = "Any process that modulates the frequency, rate or extent of "+name(x, ontology)+".";
		Set<String> synonyms = synonyms("regulation of ", x, ontology, null);
		String logicalDefinition = "cdef('GO:0065007',[regulates="+id(x, ontology)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}
	
	protected OntologyTerm negative_regulation(OWLObject x, OWLGraphWrapper ontology) {
		String id = createNewId();
		String label = "negative regulation of "+ name(x, ontology);
		String description = "Any process that modulates the frequency, rate or extent of "+name(x, ontology)+".";
		Set<String> synonyms = synonyms("negative regulation of ", x, ontology, null);
		String logicalDefinition = "cdef('GO:0065007',[negatively_regulates="+id(x, ontology)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}

	protected OntologyTerm positive_regulation(OWLObject x, OWLGraphWrapper ontology) {
		String id = createNewId();
		String label = "positive regulation of "+ name(x, ontology);
		String description = "Any process that activates or increases the frequency, rate or extent of "+name(x, ontology)+".";
		Set<String> synonyms = synonyms("positive regulation of ", x, ontology, null);
		String logicalDefinition = "cdef('GO:0065007',[positively_regulates="+id(x, ontology)+"])";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return term;
	}
}