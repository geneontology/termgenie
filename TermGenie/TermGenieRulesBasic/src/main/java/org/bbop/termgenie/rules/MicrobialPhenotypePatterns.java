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

public class MicrobialPhenotypePatterns extends Patterns {

	private final OWLGraphWrapper go;
	private final OWLGraphWrapper pato;
	private final OWLGraphWrapper omp;

	protected MicrobialPhenotypePatterns(List<OWLGraphWrapper> wrappers, DefaultTermTemplates templates) {
		super(templates.omp_entity_quality);
		this.omp = wrappers.get(0);
		this.go = wrappers.get(1);
		this.pato = wrappers.get(2);
	}

	@ToMatch
	protected List<TermGenerationOutput> omp_entity_quality(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject e = getSingleTerm(input, "entity", go);
		OWLObject q = getSingleTerm(input, "quality", pato);
		if (e == null ||  q == null) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String label = createName(name(q, pato) + " of " + name(e, go), input);
		String definition = createDefinition("Any "+name(q, pato)+" of "+name(e, go)+".", input);
		List<Synonym> synonyms = null; // TODO
		CDef cdef = new CDef(q, pato);
		cdef.addDifferentium("OBO_REL:inheres_in", e, go);
		return createTermList(label, definition, synonyms, cdef, input, omp);
	}
}
