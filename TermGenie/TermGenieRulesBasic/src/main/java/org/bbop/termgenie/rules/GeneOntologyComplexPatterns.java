package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

class GeneOntologyComplexPatterns extends Patterns {

	private final OWLObject GO0008150; // biological_process
	private final OWLObject GO0065007; // biological regulation
	private final OWLObject GO0003674; // molecular function
	private final OWLObject GO0005575; // cellular_component
	private final OWLObject GO0005488;
	private final OWLObject GO0009653;
	private final OWLObject GO0032502;
	private final OWLObject GO0043234;
	private final OWLObject PR000000001; // Protein
	private final OWLObject UBERON0001062; // http://berkeleybop.org/obo/UBERON:0001062?
	private final OWLObject PO0025131; // plant structure (An anatomical entity (CARO:0000000) that is or was part of a plant.)
	private final OWLGraphWrapper go;
	private final OWLGraphWrapper pro;
	private final OWLGraphWrapper uberon;
	private final OWLGraphWrapper plant;
	
	/**
	 * @param wrappers go, pro, uberon, plant
	 */
	protected GeneOntologyComplexPatterns(List<OWLGraphWrapper> wrappers, DefaultTermTemplates templates) {
		super(templates.all_regulation, templates.all_regulation_mf,
				templates.involved_in, templates.occurs_in,
				templates.part_of_cell_component, templates.protein_binding,
				templates.metazoan_development, templates.metazoan_morphogenesis,
				templates.plant_development, templates.plant_morphogenesis,
				templates.structural_protein_complex);
		this.go = wrappers.get(0);
		this.pro = wrappers.get(1);
		this.uberon = wrappers.get(2);
		this.plant = wrappers.get(3);
		GO0008150 = getTerm("GO:0008150", go);
		GO0065007 = getTerm("GO:0065007", go);
		GO0003674 = getTerm("GO:0003674", go);
		GO0005575 = getTerm("GO:0005575", go);
		GO0005488 = getTerm("GO:0005488", go);
		GO0009653 = getTerm("GO:0009653", go);
		GO0032502 = getTerm("GO:0032502", go);
		GO0043234 = getTerm("GO:0043234", go);
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
			List<Synonym> synonyms = synonyms("regulation of ", x, go, null, label);
			CDef cdef = new CDef(GO0065007, go);
			cdef.addDifferentium("regulates", x, go);
			createTermList(label, definition, synonyms, cdef, input, go, generatedTerms);
		}
		if (prefixes.contains("negative_regulation")) {
			String label = createName("negative regulation of "+ name(x, go), input);
			String definition = createDefinition("Any process that stops, prevents or reduces the frequency, rate or extent of "+name(x, go)+".", input);
			List<Synonym> synonyms = synonyms("negative regulation of ", x, go, null, label);
			CDef cdef = new CDef(GO0065007, go);
			cdef.addDifferentium("negatively_regulates", x, go);
			createTermList(label, definition, synonyms, cdef, input, go, generatedTerms);
		}
		if (prefixes.contains("positive_regulation")) {
			String label = createName("positive regulation of "+ name(x, go), input);
			String definition = createDefinition("Any process that activates or increases the frequency, rate or extent of "+name(x, go)+".", input);
			List<Synonym> synonyms = synonyms("positive regulation of ", x, go, null, label);
			CDef cdef = new CDef(GO0065007, go);
			cdef.addDifferentium("positively_regulates", x, go);
			createTermList(label, definition, synonyms, cdef, input, go, generatedTerms);
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
		List<Synonym> synonyms = synonyms(null, p, go, " of ", w, go, null, label);
		CDef cdef = new CDef(p, go);
		cdef.addDifferentium("part_of", w, go);
		return createTermList(label, definition, synonyms, cdef, input, go);
	}
	
	/*
	 * requires http://www.geneontology.org/scratch/xps/biological_process_xp_cellular_component.obo
	 */
	@ToMatch
	protected List<TermGenerationOutput> occurs_in(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		OWLObject p = getSingleTerm(input, "process", go);
		OWLObject c = getSingleTerm(input, "location", go);
		
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
		List<Synonym> synonyms = synonyms(null, p, go, " in ", c, go, null, label);
		CDef cdef = new CDef(p, go);
		cdef.addDifferentium("OBO_REL:occurs_in", c, go);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, p, go, " of ", w, go, null, label);
		CDef cdef = new CDef(p, go);
		cdef.addDifferentium("part_of", w, go);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, x, pro, " binding", label);
		CDef cdef = new CDef(GO0005488, go);
		cdef.addDifferentium("OBO_REL:results_in_binding_of", x, pro);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, x, uberon, " development", label);
		CDef cdef = new CDef(GO0032502, go);
		cdef.addDifferentium("OBO_REL:results_in_complete_development_of", x, uberon);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, x, uberon, " morphogenesis", label);
		CDef cdef = new CDef(GO0009653, go);
		cdef.addDifferentium("OBO_REL:results_in_morphogenesis_of", x, uberon);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, x, plant, " development", label);
		CDef cdef = new CDef(GO0032502, go);
		cdef.addDifferentium("OBO_REL:results_in_complete_development_of", x, plant);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, x, plant, " morphogenesis", label);
		CDef cdef = new CDef(GO0009653, go);
		cdef.addDifferentium("OBO_REL:results_in_morphogenesis_of", x, plant);
		return createTermList(label, definition, synonyms, cdef, input, go);
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
		List<Synonym> synonyms = synonyms(null, list, pro, "-", " complex", label);
		CDef cdef = new CDef(GO0043234, go);
		cdef.addDifferentium("has_part", list, pro);
		return createTermList(label, definition, synonyms, cdef, input, pro);
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
	
	private List<Synonym> synonyms(String prefix, List<OWLObject> list, OWLGraphWrapper ontology, String infix, String suffix, String label) {
		final int size = list.size();
		if (size == 1) {
			return synonyms(prefix, list.get(0), ontology, suffix, label);
		}
		else if (size > 1) {
			List<Synonym> prefixes = null;
			for (int i = 0; i < list.size(); i++) {
				OWLObject x = list.get(i);
				String middle;
				if (i == 0 || infix == null) {
					middle = "";
				}
				else {
					middle = infix;
				}
				prefixes = appendSynonyms(prefixes, x, ontology, middle, label);
			}
			
			List<Synonym> result;
			if (suffix == null) {
				result = new ArrayList<Synonym>(prefixes);
			} else {
				result = new ArrayList<Synonym>();
				for (Synonym prefixSynonym : prefixes) {
					StringBuilder sb = new StringBuilder(prefixSynonym.getLabel());
					sb.append(suffix);
					addSynonym(result, prefixSynonym, sb.toString(), label);
				}
			}
			return result;
		}
		return null;
	}
	
	private List<Synonym> appendSynonyms(List<Synonym> prefixes, OWLObject x, OWLGraphWrapper ontology, String infix, String label) {
		if (prefixes == null) {
			prefixes = Collections.singletonList(new Synonym("", null, null, null));
		}
		List<Synonym> oboSynonyms = ontology.getOBOSynonyms(x);
		List<Synonym> synonyms;
		String termLabel = ontology.getLabel(x);
		if (oboSynonyms == null || oboSynonyms.isEmpty()) {
			synonyms = Collections.singletonList(new Synonym(termLabel, null, null, null));
		}
		else {
			synonyms = new ArrayList<Synonym>(oboSynonyms.size() + 1);
			synonyms.addAll(oboSynonyms);
			synonyms.add(new Synonym(termLabel, null, null, null));
		}
		List<Synonym> results = new ArrayList<Synonym>(synonyms.size() * prefixes.size());
		for (Synonym prefix : prefixes) {
			for(Synonym synonym : synonyms) {
				String pScope = prefix.getScope();
				String sScope = synonym.getScope();
				if (pScope == null || pScope.equals(sScope)) {
					StringBuilder sb = new StringBuilder();
					sb.append(prefix.getLabel());
					sb.append(infix);
					sb.append(synonym.getLabel());
					addSynonym(results, synonym, sb.toString(), label);
				}
			}
		}
		return results;
	}
}