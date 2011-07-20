package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public class HumanPhenotypePatterns extends Patterns {

	private final OWLGraphWrapper hpo;
	private final OWLGraphWrapper fma;
	private final OWLGraphWrapper pato;

	protected HumanPhenotypePatterns(List<OWLGraphWrapper> wrappers, DefaultTermTemplates templates) {
		super(templates.hpo_entity_quality);
		this.hpo = wrappers.get(0);
		this.fma = wrappers.get(1);
		this.pato = wrappers.get(2);
	}

	/*
	 * requires http://compbio.charite.de/svn/hpo/trunk/human-phenotype-ontology_xp.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> hpo_entity_quality(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject e = getSingleTerm(input, "entity", fma);
		OWLObject q = getSingleTerm(input, "quality", pato);
		if (e == null || q == null) {
			return error("The specified terms do not correspond to the pattern", input);
		}
		String label = createName(name(q, pato) + " " + name(e, fma), input);
		String definition = createDefinition("Any "+name(e, fma)+" that is  "+name(q, pato)+".", input);
		List<Synonym> synonyms = null; // TODO
		CDef cdef = new CDef(q, pato); // TODO check if this the correct genus
		cdef.addDifferentium("OBO_REL:inheres_in", e, fma);
		return createTermList(label, definition, synonyms, cdef, input, hpo);
	}
}
