package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

abstract class Patterns extends BasicRules {

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
	
	protected OWLObject getSingleTerm(TermGenerationParameters parameters, TemplateField targetField, OWLGraphWrapper ontology) {
		String id = parameters.getTerms().getValue(targetField, 0).getId();
		OWLObject x = getTerm(id, ontology);
		return x ;
	}
	
	protected OWLObject getSingleTerm(TermGenerationInput input, String name, OWLGraphWrapper ontology) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		return getSingleTerm(parameters, targetField, ontology);
	}
	
	protected List<OWLObject> getListTerm(TermGenerationInput input, String name, OWLGraphWrapper ontology) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		
		int count = parameters.getTerms().getCount(targetField);
		if (count == 0) {
			return Collections.emptyList();
		}
		
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (int i = 0; i < count; i++) {
			String id = parameters.getTerms().getValue(targetField, i).getId();
			OWLObject x = getTerm(id, ontology);
			if (x != null) {
				result.add(x);
			}
		}
		return result;
	}
}