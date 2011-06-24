package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyAware.Relation;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public class HumanPhenotypePatterns extends Patterns {

	private final OWLGraphWrapper hpo;
	private final OWLGraphWrapper fma;
	private final OWLGraphWrapper pato;

	protected HumanPhenotypePatterns(OWLGraphWrapper hpo, OWLGraphWrapper fma, OWLGraphWrapper pato) {
		super(DefaultTermTemplates.hpo_entity_quality);
		this.hpo = hpo;
		this.fma = fma;
		this.pato = pato;
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
		Set<String> synonyms = null; // TODO
		String logicalDefinition = "cdef("+id(q, pato)+", ['OBO_REL:inheres_in'= "+id(e, fma)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, hpo);
	}
}
