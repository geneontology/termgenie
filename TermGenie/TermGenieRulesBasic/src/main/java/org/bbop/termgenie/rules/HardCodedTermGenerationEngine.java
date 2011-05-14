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
	
	public HardCodedTermGenerationEngine(final OWLGraphWrapper ontology) {
		goPatterns = new GeneOntologyComplexPatterns(ontology);
		hpPatterns = new HumanPhenotypePatterns(ontology);
		ompPatterns = new MicrobialPhenotypePatterns(ontology);
		clPatterns = new CellOntologyPatterns(ontology);
		uberonPatterns = new UberonPatterns(ontology);
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