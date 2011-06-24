package org.bbop.termgenie.rules;

import static org.bbop.termgenie.core.rules.DefaultTermTemplates.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyAware.Relation;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class GeneOntologyComplexPatterns extends Patterns {

	protected final OWLObject GO0008150; // biological_process
	protected final OWLObject GO0065007; // biological regulation
	protected final OWLObject GO0003674; // molecular function
	protected final OWLObject GO0005575; // cellular_component
	protected final OWLObject GO0005488; 
	protected final OWLObject GO0032991; // macromolecular complex
	protected final OWLObject PR000000001; // Protein
	protected final OWLObject UBERON0001062; // http://berkeleybop.org/obo/UBERON:0001062?
	protected final OWLObject PO0025131; // plant structure (An anatomical entity (CARO:0000000) that is or was part of a plant.)
	private final OWLGraphWrapper go;
	private final OWLGraphWrapper pro;
	private final OWLGraphWrapper uberon;
	private final OWLGraphWrapper plant;
	
	/**
	 * @param go
	 * @param pro
	 * @param uberon
	 * @param plant
	 */
	protected GeneOntologyComplexPatterns(OWLGraphWrapper go, OWLGraphWrapper pro, OWLGraphWrapper uberon, OWLGraphWrapper plant) {
		super(all_regulation, all_regulation_mf,
				involved_in, occurs_in,
				part_of_cell_component, protein_binding,
				metazoan_development, metazoan_morphogenesis,
				plant_development, plant_morphogenesis,
				structural_protein_complex);
		this.go = go;
		this.pro = pro;
		this.uberon = uberon;
		this.plant = plant;
		GO0008150 = getTerm("GO:0008150", go);
		GO0065007 = getTerm("GO:0065007", go);
		GO0003674 = getTerm("GO:0003674", go);
		GO0005575 = getTerm("GO:0005575", go);
		GO0005488 = getTerm("GO:0005488", go);
		GO0032991 = getTerm("GO:0032991", go);
		PR000000001 = getTerm("PR:000000001", pro);
		UBERON0001062 = getTerm("UBERON:0001062", uberon);
	    PO0025131 = getTerm("PO:0025131", plant);
	}
	
	protected OWLObject getTerm(String id, OWLGraphWrapper ontology) {
		OWLObject term = getTermSimple(id, ontology);
		if (term == null) {
			throw new RuntimeException("No term found for id: "+id);
		}
		return term;
	}
	
	@ToMatch
	protected List<TermGenerationOutput> all_regulation(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		return allRegulationTemplate(input, pending, GO0008150);
	}
	
	@ToMatch
	protected List<TermGenerationOutput> all_regulation_mf(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		return allRegulationTemplate(input, pending, GO0003674);
	}
	
	protected List<TermGenerationOutput> allRegulationTemplate(TermGenerationInput input, Map<String, OntologyTerm> pending, OWLObject branch) {
		OWLObject x = getSingleTerm(input, "target", go);
		
		CheckResult check = checkGenus(x, branch, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		if (genus(x, GO0065007, go)) {
			// 	biological regulation
			// TODO restriction should only used asserted links, not implied ones
			return error("Cannot create 'regulation of regulation of X' terms. The term "+getTermShortInfo(GO0065007, go)+" is a parent of "+getTermShortInfo(x, go), input);
		}
		List<String> prefixes = getFieldStringList(input, "target");
		if (prefixes == null || prefixes.isEmpty()) {
			return error("Could not create a term for X, as no prefix was selected", input);
		}
		
		List<TermGenerationOutput> generatedTerms = new ArrayList<TermGenerationOutput>(prefixes.size());
		if (prefixes.contains("regulation")) {
			String label = createName("regulation of "+ name(x, go), input);
			String definition = createDefinition("Any process that modulates the frequency, rate or extent of "+name(x, go)+".", input);
			Set<String> synonyms = synonyms("regulation of ", x, go, null, label);
			String logicalDefinition = "cdef('GO:0065007', [regulates= "+id(x, go)+"])";
			List<Relation> relations = null; // TODO create code
			createTermList(label, definition, synonyms, logicalDefinition, relations, input, go, generatedTerms);
		}
		if (prefixes.contains("negative_regulation")) {
			String label = createName("negative regulation of "+ name(x, go), input);
			String definition = createDefinition("Any process that stops, prevents or reduces the frequency, rate or extent of "+name(x, go)+".", input);
			Set<String> synonyms = synonyms("negative regulation of ", x, go, null, label);
			String logicalDefinition = "cdef('GO:0065007', [negatively_regulates= "+id(x, go)+"])";
			List<Relation> relations = null; // TODO create code
			createTermList(label, definition, synonyms, logicalDefinition, relations, input, go, generatedTerms);
		}
		if (prefixes.contains("positive_regulation")) {
			String label = createName("positive regulation of "+ name(x, go), input);
			String definition = createDefinition("Any process that activates or increases the frequency, rate or extent of "+name(x, go)+".", input);
			Set<String> synonyms = synonyms("positive regulation of ", x, go, null, label);
			String logicalDefinition = "cdef('GO:0065007', [positively_regulates= "+id(x, go)+"])";
			List<Relation> relations = null; // TODO create code
			createTermList(label, definition, synonyms, logicalDefinition, relations, input, go, generatedTerms);
		}
		if (generatedTerms.isEmpty()) {
			return error("Could not create a term for X, as no known prefix was selected", input);
		}
		return generatedTerms;
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_self.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> involved_in(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "part", go);
		OWLObject w = getSingleTerm(input, "whole", go);
		
		CheckResult check = checkGenus(p, GO0008150, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		check = checkGenus(w, GO0008150, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(p, go) + " involved in " + name(w, go), input);
		String definition = createDefinition("Any "+name(p, go)+" that is involved in "+name(w, go)+".", input);
		Set<String> synonyms = synonyms(null, p, go, " of ", w, go, null, label);
		String logicalDefinition = "cdef("+id(p, go)+", [part_of= "+id(w, go)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_cellular_component.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> occurs_in(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "process", go);
		OWLObject c = getSingleTerm(input, "whole", go);
		
		CheckResult check = checkGenus(p, GO0008150, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		check = checkGenus(c, GO0005575, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(p, go) + " in " + name(c, go), input);
		String definition = createDefinition("Any "+name(p, go)+" that takes place in "+name(c, go)+".", input);
		Set<String> synonyms = synonyms(null, p, go, " in ", c, go, null, label);
		String logicalDefinition = "cdef("+id(p, go)+", ['OBO_REL:occurs_in'= "+id(c, go)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}

	@ToMatch
	protected List<TermGenerationOutput> part_of_cell_component(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "part", go);
		OWLObject w = getSingleTerm(input, "whole", go);
		
		CheckResult check = checkGenus(p, GO0005575, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		check = checkGenus(w, GO0005575, go, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(p, go) + " " + name(w, go), input);
		String definition = createDefinition("Any "+name(p, go)+" that is part of a "+name(w, go)+".", input);
		Set<String> synonyms = synonyms(null, p, go, " of ", w, go, null, label);
		String logicalDefinition = "cdef("+id(p, go)+", [part_of= "+id(w, go)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}

	/*
	 * requires http://www.geneontology.org/scratch/xps/molecular_function_xp_protein.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> protein_binding(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target", pro);
		
		CheckResult check = checkGenus(x, PR000000001, pro, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(x, pro) + " binding", input);
		String definition = createDefinition("Interacting selectively and non-covalently with  "+name(x, pro)+".", input);
		Set<String> synonyms = synonyms(null, x, pro, " binding", label);
		String logicalDefinition = "cdef('GO:0005488', ['OBO_REL:results_in_binding_of'= "+id(x, pro)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}

	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo'],
	 */
	@ToMatch
	protected List<TermGenerationOutput> metazoan_development(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target", uberon);
		
		CheckResult check = checkGenus(x, UBERON0001062, uberon, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(x, uberon) + " development", input);
		String definition = createDefinition("The process whose specific outcome is the progression of "+refname(x, uberon)+" over time, from its formation to the mature structure.", input);
		Set<String> synonyms = synonyms(null, x, uberon, " development", label);
		String logicalDefinition = "cdef('GO:0032502', ['OBO_REL:results_in_complete_development_of'= "+id(x, uberon)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}

	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_uber_anatomy.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> metazoan_morphogenesis(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target", uberon);
		
		CheckResult check = checkGenus(x, UBERON0001062, uberon, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(x, uberon) + " morphogenesis", input);
		String definition = createDefinition("The developmental process by which "+refname(x, uberon)+" is generated and organized.", input);
		Set<String> synonyms = synonyms(null, x, uberon, " morphogenesis", label);
		String logicalDefinition = "cdef('GO:0009653', ['OBO_REL:results_in_morphogenesis_of'= "+id(x, uberon)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}

	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> plant_development(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target", plant);
		
		CheckResult check = checkGenus(x, PO0025131, plant, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(x, plant) + " development", input);
		String definition = createDefinition("The process whose specific outcome is the progression of "+refname(x, plant)+" over time, from its formation to the mature structure.", input);
		Set<String> synonyms = synonyms(null, x, plant, " development", label);
		String logicalDefinition = "cdef('GO:0032502', ['OBO_REL:results_in_complete_development_of'= "+id(x, plant)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_plant_anatomy.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> plant_morphogenesis(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject x = getSingleTerm(input, "target", plant);
		
		CheckResult check = checkGenus(x, PO0025131, plant, input);
		if (check.isGenus == false) {
			return check.error;
		}
		
		String label = createName(name(x, plant) + " morphogenesis", input);
		String definition = createDefinition("The developmental process by which "+refname(x, plant)+" is generated and organized.", input);
		Set<String> synonyms = synonyms(null, x, plant, " morphogenesis", label);
		String logicalDefinition = "cdef('GO:0009653', ['OBO_REL:results_in_morphogenesis_of'= "+id(x, plant)+"])";
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, go);
	}
	
	/*
	 * requires= http://www.geneontology.org/scratch/xps/cellular_component_xp_protein.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> structural_protein_complex(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		List<OWLObject> list = getListTerm(input, "unit", pro);
		for (OWLObject x : list) {
			CheckResult check = checkGenus(x, PR000000001, pro, input);
			if (check.isGenus == false) {
				return check.error;
			}
		}
		
		String label = createName(null, list, pro, "-"," complex", input);
		String definition = createDefinition("Any protein complex consisting of ",list, pro, ", ",".", input);
		Set<String> synonyms = synonyms(null, list, pro, "-", " complex", label);
		String logicalDefinition = createCDef("cdef('GO:0043234', [",list, pro, "has_part= ", ", ", "])");
		List<Relation> relations = null; // TODO create code
		return createTermList(label, definition, synonyms, logicalDefinition, relations, input, pro);
	}
	
	private String createName(String prefix, List<OWLObject> list, OWLGraphWrapper ontology, String infix, String suffix, TermGenerationInput input) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(infix);
			}
			sb.append(name(list.get(i), ontology));
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		return createName(sb.toString(), input);
	}
	
	private Set<String> synonyms(String prefix, List<OWLObject> list, OWLGraphWrapper ontology, String infix, String suffix, String label) {
		final int size = list.size();
		if (size == 1) {
			return synonyms(prefix, list.get(0), ontology, suffix, label);
		}
		else if (size > 1) {
			List<String> prefixes = Collections.singletonList(prefix == null ? "" : prefix);
			for (int i = 0; i < list.size(); i++) {
				OWLObject x = list.get(i);
				String middle;
				if (i == 0 || infix == null) {
					middle = "";
				}
				else {
					middle = infix;
				}
				prefixes = appendSynonyms(prefixes, x, ontology, middle);
			}
			
			Set<String> result;
			if (suffix == null) {
				result = new HashSet<String>(prefixes);
			} else {
				result = new HashSet<String>();
				for (String string : prefixes) {
					result.add(string+suffix);
				}
			}
			result.remove(label);
			return result;
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	private List<String> appendSynonyms(List<String> prefixes, OWLObject x, OWLGraphWrapper ontology, String infix) {
		String[] synonymStrings = ontology.getSynonymStrings(x);
		List<String> synonyms;
		String label = ontology.getLabel(x);
		if (synonymStrings == null || synonymStrings.length == 0) {
			synonyms = Collections.singletonList(label);
		}
		else {
			synonyms = new ArrayList<String>(synonymStrings.length + 1);
			synonyms.addAll(Arrays.asList(synonymStrings));
			synonyms.add(label);
		}
		List<String> results = new ArrayList<String>(synonyms.size() * prefixes.size());
		for (String prefix : prefixes) {
			for(String synonym : synonyms) {
				results.add(prefix + infix + synonym);
			}
		}
		return results;
	}
}