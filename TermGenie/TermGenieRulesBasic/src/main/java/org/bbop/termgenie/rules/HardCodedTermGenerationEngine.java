package org.bbop.termgenie.rules;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

import owltools.graph.OWLGraphWrapper;

public class HardCodedTermGenerationEngine extends DefaultTermTemplates implements TermGenerationEngine {

	private final GeneOntologyComplexPatterns goPatterns;
	private final HumanPhenotypePatterns hpPatterns;
	private final MicrobialPhenotypePatterns ompPatterns;
	private final CellOntologyPatterns clPatterns;
	private final UberonPatterns uberonPatterns;
	
	public HardCodedTermGenerationEngine(List<Ontology> ontologies) {
		OWLGraphWrapper go = null;
		OWLGraphWrapper pro = null;
		OWLGraphWrapper uberon = null;
		OWLGraphWrapper plant = null;
		OWLGraphWrapper hpo = null;
		OWLGraphWrapper fma = null;
		OWLGraphWrapper pato = null;
		OWLGraphWrapper cell = null;
		OWLGraphWrapper omp = null;
		
		for (Ontology ontology : ontologies) {
			OWLGraphWrapper instance = ontology.getRealInstance();
			if (instance != null) {
				if (equals(GENE_ONTOLOGY, ontology)) {
					go = instance;
				}
				else if (equals(PROTEIN_ONTOLOGY, ontology)) {
					pro = instance;
				}
				else if (equals(UBERON_ONTOLOGY, ontology)) {
					uberon = instance;
				}
				else if (equals(PLANT_ONTOLOGY, ontology)) {
					plant = instance;
				}
				else if (equals(HP_ONTOLOGY, ontology)) {
					hpo = instance;
				}
				else if (equals(FMA_ONTOLOGY, ontology)) {
					fma = instance;
				}
				else if (equals(PATO, ontology)) {
					pato = instance;
				}
				else if (equals(CELL_ONTOLOGY, ontology)) {
					cell = instance;
				}
				else if (equals(OMP, ontology)) {
					omp = instance;
				}
			}
		}
		
		goPatterns = new GeneOntologyComplexPatterns(go, pro, uberon, plant);
		hpPatterns = new HumanPhenotypePatterns(hpo, fma, pato);
		ompPatterns = new MicrobialPhenotypePatterns(omp, go, pato);
		clPatterns = new CellOntologyPatterns(cell, uberon, pro, go);
		uberonPatterns = new UberonPatterns(uberon);
	}
	
	@Override
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		if (ontology == null || generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		if (equals(GENE_ONTOLOGY, ontology)) {
			return goPatterns.generateTerms(ontology, generationTasks);
		}
		else if (equals(HP_ONTOLOGY, ontology)) {
			return hpPatterns.generateTerms(ontology, generationTasks);
		}
		else if (equals(OMP, ontology)) {
			return ompPatterns.generateTerms(ontology, generationTasks);
		}
		else if (equals(CELL_ONTOLOGY, ontology)) {
			return clPatterns.generateTerms(ontology, generationTasks);
		}
		else if (equals(UBERON_ONTOLOGY, ontology)) {
			return uberonPatterns.generateTerms(ontology, generationTasks);
		}
		// TODO decide if to set error message for unknown ontology
		return null;	
	}
	
	private static boolean equals(Ontology o1, Ontology o2) {
		if (o1 == o2) {
			return true;
		}
		return o1.getUniqueName().equals(o2.getUniqueName());
	}
}