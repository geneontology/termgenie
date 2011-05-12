package org.bbop.termgenie.rules;

import java.util.Set;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class Patterns extends BasicRules {

	/**
	 * @param ontology
	 */
	protected Patterns(OWLGraphWrapper ontology) {
		super(ontology);
	}
	
	
	protected OWLObject getSingleTerm(TermGenerationParameters parameters, TemplateField targetField) {
		String id = parameters.getTerms().getValue(targetField, 0).getId();
		OWLObject x = ontology.getOWLObjectByIdentifier(id);
		return x ;
	}
	
	protected OWLObject getSingleTerm(TermGenerationInput input, String name) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		return getSingleTerm(parameters, targetField);
	}
	
	protected boolean genus(OWLObject x, OWLObject parent) {
		Set<OWLObject> descendants = ontology.getDescendantsReflexive(parent);
		return descendants.contains(x);
	}
}