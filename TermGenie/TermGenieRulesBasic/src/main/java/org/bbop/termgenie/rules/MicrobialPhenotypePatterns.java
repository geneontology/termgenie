package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

import owltools.graph.OWLGraphWrapper;

public class MicrobialPhenotypePatterns extends Patterns {

	private final OWLGraphWrapper go;
	private final OWLGraphWrapper pato;
	private final OWLGraphWrapper omp;

	protected MicrobialPhenotypePatterns(OWLGraphWrapper omp, OWLGraphWrapper go, OWLGraphWrapper pato) {
		this.omp = omp;
		this.go = go;
		this.pato = pato;
	}

	@Override
	protected List<TermGenerationOutput> generate(TermGenerationInput input,
			Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	template(omp_entity_quality(E,Q),
	         [
	          ontology= 'OMP',
	          obo_namespace= omp,
	          description= 'basic EQ template',
	          externals= ['GO','PATO'],
	          arguments= [entity='GO', quality='PATO'],
	          cdef= cdef(Q,['OBO_REL:inheres_in'=E]),
	          name= [name(Q),' of ',name(E)],
	          def= ['Any ',name(Q),' of ',name(E)]
	         ]).
	         */
}
