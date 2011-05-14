package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

import owltools.graph.OWLGraphWrapper;

public class HumanPhenotypePatterns extends Patterns {

	protected HumanPhenotypePatterns(OWLGraphWrapper ontology) {
		super(ontology);
	}

	@Override
	protected List<TermGenerationOutput> generate(TermGenerationInput input,
			Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	template(hpo_entity_quality(E,Q),
         [
          ontology= 'HP',
          obo_namespace= medical_genetics,
          description= 'basic EQ template',
          externals= ['FMA','PATO'],
          requires= ['http://compbio.charite.de/svn/hpo/trunk/human-phenotype-ontology_xp.obo'],
          arguments= [entity='FMA', quality='PATO'],
          cdef= cdef(Q,['OBO_REL:inheres_in'=E]),
          name= [name(Q),' ',name(E)],
          def= ['Any ',name(E),' that is ',name(Q)]
         ]).
	 */
}
