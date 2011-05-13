package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class GeneOntologyComplexPatterns extends PrivatePatterns {

	protected final OWLObject GO0008150; // biological_process
	protected final OWLObject GO0065007; // biological regulation
	protected final OWLObject GO0003674; // molecular function
	protected final OWLObject GO0005575; // cellular_component
	protected final OWLObject GO0005488; 
	protected final OWLObject PR000000001; // Protein
	protected final OWLObject UBERON0001062; // http://berkeleybop.org/obo/UBERON:0001062?
	
	protected static List<TermGenerationOutput> error(String message, TermGenerationInput input) {
		TermGenerationOutput output = new TermGenerationOutput(null, input, false, "Cannot create 'regulation of non biological process X' term");
		return Collections.singletonList(output);
	}

	protected static TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}

	/**
	 * @param ontology
	 */
	protected GeneOntologyComplexPatterns(OWLGraphWrapper ontology) {
		super(ontology);
		GO0008150 = getTerm("GO:0008150");
		GO0065007 = getTerm("GO:0065007");
		GO0003674 = getTerm("GO:0003674");
		GO0005575 = getTerm("GO:0005575");
		GO0005488 = getTerm("GO:0005488");
		PR000000001 = getTerm("PR:000000001");
		UBERON0001062 = getTerm("UBERON:0001062");
	}
	
	private OWLObject getTerm(String id) {
		OWLObject term = ontology.getOWLObjectByIdentifier("UBERON:0001062");
		if (term == null) {
			throw new RuntimeException("No term found for id: "+id);
		}
		return term;
	}
	
	public List<TermGenerationOutput> all_regulation(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		return allRegulationTemplate(input, pending, GO0008150);
	}
	
	public List<TermGenerationOutput> all_regulation_of_mf(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		return allRegulationTemplate(input, pending, GO0003674);
	}
	
	private List<TermGenerationOutput> allRegulationTemplate(TermGenerationInput input, Map<String, OntologyTerm> pending, OWLObject branch) {
		TemplateField targetField = input.getTermTemplate().getField("target");
		TermGenerationParameters parameters = input.getParameters();
		OWLObject x = getSingleTerm(input, "target");
		if (!genus(x, branch)) {
			// check branch
			return error("The specified term does not correspond to the patterns", input);
		}
		if (genus(x, GO0065007)) {
			// 	biological regulation
			return error("Cannot create 'regulation of regulation of X' terms", input);
		}
		List<String> prefixes = parameters.getPrefixes().getValue(targetField, 0);
		if (prefixes == null || prefixes.isEmpty()) {
			return error("Could not create a term for X, as no prefix was selected", input);
		}
		
		List<TermGenerationOutput> generatedTerms = new ArrayList<TermGenerationOutput>(prefixes.size());
		if (prefixes.contains("regulation")) {
			OntologyTerm term = regulation(x);
			if (term != null) {
				generatedTerms.add(success(term, input));
			}
		}
		if (prefixes.contains("negative_regulation")) {
			OntologyTerm term = negative_regulation(x);
			if (term != null) {
				generatedTerms.add(success(term, input));
			}
		}
		if (prefixes.contains("positive_regulation")) {
			OntologyTerm term = positive_regulation(x);
			if (term != null) {
				generatedTerms.add(success(term, input));
			}
		}
		if (generatedTerms.isEmpty()) {
			return error("Could not create a term for X, as no known prefix was selected", input);
		}
		return generatedTerms;
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_self.obo
	 */
	public List<TermGenerationOutput> involved_in(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "part");
		OWLObject w = getSingleTerm(input, "whole");
		if (!genus(p, GO0008150) || !genus(w, GO0008150)) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String id = createNewId();
		String label = name(p) + " involved in " + name(w);
		String description = "Any "+name(p)+" that is involved in "+name(w)+".";
		Set<String> synonyms = synonyms(null, p, " of ", w, null);
		String logicalDefinition = "cdef("+id(p)+",[part_of="+id(w)+"]),";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return Collections.singletonList(success(term, input));
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_cellular_component.obo
	 */
	public List<TermGenerationOutput> takes_place_in(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "process");
		OWLObject c = getSingleTerm(input, "whole");
		if (!genus(p, GO0008150) || !genus(c, GO0005575)) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String id = createNewId();
		String label = name(p) + " in " + name(c);
		String description = "Any "+name(p)+" that takes place in "+name(c)+".";
		Set<String> synonyms = synonyms(null, p, " in ", c, null);
		String logicalDefinition = "cdef("+id(p)+",['OBO_REL:occurs_in'="+id(c)+"]),";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return Collections.singletonList(success(term, input));
	}

	public List<TermGenerationOutput> part_of_cell_component(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "part");
		OWLObject w = getSingleTerm(input, "whole");
		if (!genus(p, GO0005575) || !genus(w, GO0005575)) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String id = createNewId();
		String label = name(p) + " " + name(w);
		String description = "Any "+name(p)+" that is part of a "+name(w)+".";
		Set<String> synonyms = synonyms(null, p, " of ", w, null);
		String logicalDefinition = "cdef("+id(p)+",[part_of="+id(w)+"]),";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return Collections.singletonList(success(term, input));
	}

	/*
	 * requires http://www.geneontology.org/scratch/xps/molecular_function_xp_protein.obo
	 */
	public List<TermGenerationOutput> protein_binding(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target");
		if (!genus(x, PR000000001)) {
			// check if protein ontology [PRO]
			return error("The specified terms do not correspond to the pattern", input);
		}
		String id = createNewId();
		String label = name(x) + " binding";
		String description = "Interacting selectively and non-covalently with  "+name(x)+".";
		Set<String> synonyms = synonyms(x, null, " binding");
		String logicalDefinition = "cdef('GO:0005488',['OBO_REL:results_in_binding_of'="+id(x)+"]),";
		OntologyTerm term = new OntologyTerm.DefaultOntologyTerm(id, label, description, synonyms, logicalDefinition);
		return Collections.singletonList(success(term, input));
	}

	/*
	template(metazoan_development(X),
         [
          description= 'development of an animal anatomical structure',
          ontology= 'GO',
          obo_namespace= biological_process,
          externals= ['UBERON'],
          requires= ['http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo'],
          arguments= [target='UBERON'],
          cdef= cdef('GO:0032502',['OBO_REL:results_in_complete_development_of'=X]),
          name= [name(X),' development'],
          synonyms= [[synonym(X),' development']],
          def= ['The process whose specific outcome is the progression of ',refname(X),' over time, from its formation to the mature structure.']
                %def([' A ',name(X),' is '],X,'.')
         ]).
	 */
	public List<TermGenerationOutput> metazoan_development(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	template(metazoan_morphogenesis(X),
         [
          description= 'morphogenesis of an animal anatomical structure',
          ontology= 'GO',
          obo_namespace= biological_process,
          externals= ['UBERON'],
          requires= ['http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo'],
          arguments= [target='UBERON'],
          cdef= cdef('GO:0009653',['OBO_REL:results_in_morphogenesis_of'=X]),
          name= [name(X),' morphogenesis'],
          synonyms= [[synonym(X),' morphogenesis']],
          def= ['The developmental process by which ',refname(X),' is generated and organized.']
         ]).
	 */
	public List<TermGenerationOutput> metazoan_morphogenesis(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	template(plant_development(X),
         [
          description= 'development of a plant anatomical structure',
          ontology= 'GO',
          obo_namespace= biological_process,
          externals= ['PO'],
          requires= ['http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo'],
          arguments= [target='UBERON'],
          cdef= cdef('GO:0032502',['OBO_REL:results_in_complete_development_of'=X]),
          name= [name(X),' development'],
          synonyms= [[synonym(X),' development']],
          def= ['The process whose specific outcome is the progression of ',refname(X),' over time, from its formation to the mature structure.']
                %def([' A ',name(X),' is '],X,'.')
         ]).

	 */
	public List<TermGenerationOutput> plant_development(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	template(plant_morphogenesis(X),
         [
          description= 'morphogenesis of a plant animal anatomical structure',
          ontology= 'GO',
          obo_namespace= biological_process,
          externals= ['PO'],
          requires= ['http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo'],
          arguments= [target='UBERON'],
          cdef= cdef('GO:0009653',['OBO_REL:results_in_morphogenesis_of'=X]),
          name= [name(X),' morphogenesis'],
          synonyms= [[synonym(X),' morphogenesis']],
          def= ['The developmental process by which ',refname(X),' is generated and organized.']
         ]).
	 */
	public List<TermGenerationOutput> plant_morphogenesis(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		// TODO Auto-generated method stub
		return null;
	}
}