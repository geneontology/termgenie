package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

import owltools.graph.OWLGraphWrapper;

public class CellOntologyPatterns extends Patterns {

	private final OWLGraphWrapper cell;
	private final OWLGraphWrapper uberon;
	private final OWLGraphWrapper pro;
	private final OWLGraphWrapper go;

	protected CellOntologyPatterns(OWLGraphWrapper cell, OWLGraphWrapper uberon, OWLGraphWrapper pro, OWLGraphWrapper go) {
		this.cell = cell;
		this.uberon = uberon;
		this.pro = pro;
		this.go = go;
	}
	
	@Override
	protected List<TermGenerationOutput> generate(TermGenerationInput input,
			Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	template(metazoan_location_specific_cell(C,A),
	         [
	          ontology= 'CL',
	          obo_namespace= cell,
	          description= 'A cell type differentiated by its anatomical location (animals)',
	          externals= ['UBERON'],
	          arguments= [cell='CL',location='UBERON'],
	          cdef= cdef(C,[part_of=A]),
	          name= [name(A),' ',name(C)],
	          def= ['Any ',name(C),' that is part of a ',name(A),'.']
	         ]).

	template(cell_by_surface_marker(C,P),
	         [
	          ontology= 'CL',
	          obo_namespace= cell,
	          description= 'A cell type differentiated by proteins or complexes on the plasma membrane',
	          externals= ['PRO','GO'],
	          arguments= [cell='CL',membrane_part=['PRO','GO:0032991']],
	          cdef= cdef(C,[has_plasma_membrane_part=P]),
	          properties = [multivalued(has_plasma_membrane_part),
	                        any_cardinality(has_plasma_membrane_part)],
	          name= [names(P),' ',name(C)],
	          def= ['Any ',name(C),' that has ',names(P),' on the plasma membrane']
	         ]).
	         */
}
