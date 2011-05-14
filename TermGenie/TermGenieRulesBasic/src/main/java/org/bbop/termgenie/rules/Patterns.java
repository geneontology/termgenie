package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

abstract class Patterns extends BasicRules {

	/**
	 * @param ontology
	 */
	protected Patterns(OWLGraphWrapper ontology) {
		super(ontology);
	}
	
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		List<TermGenerationOutput> result = new ArrayList<TermGenerationOutput>();
		Map<String, OntologyTerm> pending = new HashMap<String, OntologyTerm>();
		for (TermGenerationInput input : generationTasks) {
			List<TermGenerationOutput> output = generate(input, pending);
			if (output != null && !output.isEmpty()) {
				result.addAll(output);
			}
		}
		return result;
	}
	
	protected abstract List<TermGenerationOutput> generate(TermGenerationInput input, Map<String, OntologyTerm> pending);
	
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
	
	protected List<OWLObject> getListTerm(TermGenerationInput input, String name) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		
		int count = parameters.getTerms().getCount(targetField);
		if (count == 0) {
			return Collections.emptyList();
		}
		
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (int i = 0; i < count; i++) {
			String id = parameters.getTerms().getValue(targetField, i).getId();
			OWLObject x = ontology.getOWLObjectByIdentifier(id);
			if (x != null) {
				result.add(x);
			}
		}
		return result;
	}
	
	protected boolean genus(OWLObject x, OWLObject parent) {
		Set<OWLObject> descendants = ontology.getDescendantsReflexive(parent);
		return descendants.contains(x);
	}
}