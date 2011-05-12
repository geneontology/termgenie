package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

import owltools.graph.OWLGraphWrapper;

public class HardCodedTermGenerationEngine extends DefaultTermTemplates implements TermGenerationEngine {

	private final OWLGraphWrapper ontology;
	private final GeneOntologyComplexPatterns patterns;
	
	public HardCodedTermGenerationEngine() {
		ontology = GENE_ONTOLOGY.getRealInstance();
		if (ontology == null) {
			throw new RuntimeException("GeneOntology may not be null.");
		}
		patterns = new GeneOntologyComplexPatterns(ontology);
	}
	
	@Override
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		if (ontology == null || generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		if (GENE_ONTOLOGY.getUniqueName().equals(ontology.getUniqueName())) {
			return generateGOTerms(ontology, generationTasks);
		}
		// take care of other templates with non GeneOntology targets.
		/*
		 * template(hpo_entity_quality(E,Q),
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

template(structural_protein_complex(X,Y),
         [
          description= 'protein complex defined structurally',
          ontology= 'GO',
          obo_namespace= cellular_component,
          externals= ['PRO'],
          %requires= ['http://www.geneontology.org/scratch/xps/cellular_component_xp_protein.obo'],
          arguments= [unit1='PRO',unit2='PRO'],
          cdef= cdef('GO:0043234',[has_part=X,has_part=Y]),
          name= [name(X),'-',name(Y),' complex'],
          synonyms= [[synonym(X),'-',synonym(Y),' complex']],
          def= ['Any protein complex consisting of a',name(X),' and a ',name(Y),'.']
         ]).

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
		return null;	
	}
	
	protected List<TermGenerationOutput> generateGOTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		List<TermGenerationOutput> result = new ArrayList<TermGenerationOutput>();
		Map<String, OntologyTerm> pending = new HashMap<String, OntologyTerm>();
		for (TermGenerationInput input : generationTasks) {
			TermTemplate template = input.getTermTemplate();
			List<TermGenerationOutput> output = null;
			if (equals(all_regulation, template)) {
				output = patterns.all_regulation(input, pending);
			}
			else if (equals(all_regulation_mf, template)) {
				output = patterns.all_regulation_of_mf(input, pending);
			}
			else if (equals(involved_in, template)) {
				output = patterns.involved_in(input, pending);
			}
			else if (equals(takes_place_in, template)) {
				output = patterns.takes_place_in(input, pending);
			}
			else if (equals(part_of_cell_component, template)) {
				output = patterns.part_of_cell_component(input, pending);
			}
			else if (equals(protein_binding, template)) {
				output = patterns.protein_binding(input, pending);
			}
			else if (equals(metazoan_development, template)) {
				output = patterns.metazoan_development(input, pending);
			}
			else if (equals(metazoan_morphogenesis, template)) {
				output = patterns.metazoan_morphogenesis(input, pending);
			}
			else if (equals(plant_development, template)) {
				output = patterns.plant_development(input, pending);
			}
			else if (equals(plant_morphogenesis, template)) {
				output = patterns.plant_morphogenesis(input, pending);
			}
			if (output != null && !output.isEmpty()) {
				result.addAll(output);
			}
		}
		return result;
	}
	
	private static boolean equals(TermTemplate t1, TermTemplate t2) {
		return t1.getName().equals(t2.getName());
	}
}