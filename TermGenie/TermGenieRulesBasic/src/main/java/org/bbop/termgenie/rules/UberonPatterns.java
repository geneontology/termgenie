package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

import owltools.graph.OWLGraphWrapper;

public class UberonPatterns extends Patterns {

	protected UberonPatterns(OWLGraphWrapper ontology) {
		super(ontology);
	}

	@Override
	protected List<TermGenerationOutput> generate(TermGenerationInput input,
			Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	/*
	template(metazoan_location_specific_anatomical_structure(P,W),
	         [
	          access= [anyone],
	          description= 'location-specific anatomical structure',
	          ontology= 'UBERON',
	          obo_namespace= uberon,
	          arguments= [part='UBERON',whole='UBERON'],
	          cdef= cdef(P,[part_of=W]),
	          name= [name(W),' ',name(P)],
	          synonyms= [[synonym(P),' of ',synonym(W)]],
	          def= ['Any ',name(P),' that is part of a ',name(W),'.']
	         ]).
			 */
}
