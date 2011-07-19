package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public class UberonPatterns extends Patterns {

	private final OWLGraphWrapper uberon;

	protected UberonPatterns(List<OWLGraphWrapper> wrappers, DefaultTermTemplates templates) {
		super(templates.metazoan_location_specific_anatomical_structure);
		this.uberon = wrappers.get(0);
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
		List<Synonym> synonyms = synonyms(null, p, uberon, " of ", w, uberon, null, label);
		CDef logicalDefinition = new CDef(p, uberon);
		logicalDefinition.addDifferentium("part_of", w, uberon);
		return createTermList(label, definition, synonyms, logicalDefinition, input, uberon);
	}
}
