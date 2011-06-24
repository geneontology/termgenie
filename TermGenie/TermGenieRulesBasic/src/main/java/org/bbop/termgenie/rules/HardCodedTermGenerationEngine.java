package org.bbop.termgenie.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

import owltools.graph.OWLGraphWrapper;

public class HardCodedTermGenerationEngine extends DefaultTermTemplates implements TermGenerationEngine {

	private final Map<String, Patterns> patterns;

	public HardCodedTermGenerationEngine(List<? extends Ontology> ontologies) {
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
		patterns = new HashMap<String, Patterns>();
		patterns.put(GENE_ONTOLOGY.getUniqueName(), new GeneOntologyComplexPatterns(go, pro, uberon, plant));
		patterns.put(HP_ONTOLOGY.getUniqueName(), new HumanPhenotypePatterns(hpo, fma, pato));
		patterns.put(OMP.getUniqueName(), new MicrobialPhenotypePatterns(omp, go, pato));
		patterns.put(CELL_ONTOLOGY.getUniqueName(), new CellOntologyPatterns(cell, uberon, pro, go));
		patterns.put(UBERON_ONTOLOGY.getUniqueName(), new UberonPatterns(uberon));
	}
	
	@Override
	public List<TermTemplate> getAvailableTemplates() {
		return defaultTemplates;
	}

	@Override
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		if (ontology == null || generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		Patterns patterns = this.patterns.get(ontology.getUniqueName());
		if (patterns != null) {
			List<TermGenerationOutput> terms = patterns.generateTerms(ontology, generationTasks);
			return terms;
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