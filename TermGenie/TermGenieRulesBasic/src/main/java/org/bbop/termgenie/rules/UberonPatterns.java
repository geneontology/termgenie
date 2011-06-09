package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public class UberonPatterns extends Patterns {

	private final OWLGraphWrapper uberon;

	protected UberonPatterns(OWLGraphWrapper uberon) {
		super(DefaultTermTemplates.metazoan_location_specific_anatomical_structure);
		this.uberon = uberon;
	}

	@ToMatch
	protected List<TermGenerationOutput> metazoan_location_specific_anatomical_structure(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "part", uberon);
		OWLObject w = getSingleTerm(input, "whole", uberon);
		if (p == null || w == null) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String label = createName(name(w, uberon) + " " + name(p, uberon), input);
		String definition = createDefinition("Any "+name(p, uberon)+" that is part of a "+name(w, uberon)+".", input);
		Set<String> synonyms = synonyms(null, p, uberon, " of ", w, uberon, null);
		String logicalDefinition = "cdef("+id(p, uberon)+",[part_of="+id(w, uberon)+"]),";
		return createTermList(label, definition, synonyms, logicalDefinition, input, uberon);
	}
}
