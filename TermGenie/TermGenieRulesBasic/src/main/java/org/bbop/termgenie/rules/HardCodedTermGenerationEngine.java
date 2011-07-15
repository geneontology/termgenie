package org.bbop.termgenie.rules;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;

import owltools.graph.OWLGraphWrapper;

public class HardCodedTermGenerationEngine extends DefaultTermTemplates implements TermGenerationEngine {

	private final Map<String, PatternInstance> patterns;

	public HardCodedTermGenerationEngine(MultiOntologyTaskManager manager) {
		patterns = new HashMap<String, PatternInstance>();
		patterns.put(GENE_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					GeneOntologyComplexPatterns.class, 
					GENE_ONTOLOGY, PROTEIN_ONTOLOGY, UBERON_ONTOLOGY, PLANT_ONTOLOGY));
		patterns.put(HP_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					HumanPhenotypePatterns.class, 
					HP_ONTOLOGY, FMA_ONTOLOGY, PATO));
		patterns.put(OMP.getUniqueName(), 
				new PatternInstance(manager,
					MicrobialPhenotypePatterns.class, 
					OMP, GENE_ONTOLOGY, PATO));
		patterns.put(CELL_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					CellOntologyPatterns.class, 
					CELL_ONTOLOGY, UBERON_ONTOLOGY, PROTEIN_ONTOLOGY, GENE_ONTOLOGY));
		patterns.put(UBERON_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					UberonPatterns.class, 
					UBERON_ONTOLOGY));
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
		PatternInstance instance = this.patterns.get(ontology.getUniqueName());
		if (instance != null) {
			List<TermGenerationOutput> terms = instance.run(ontology, generationTasks);
			return terms;
		}
		// TODO decide if to set error message for unknown ontology
		return null;	
	}
	
	
	private static class PatternInstance {
		
		private final Class<? extends Patterns> c;
		private final Ontology[] ontologies;
		private final MultiOntologyTaskManager manager;

		PatternInstance(MultiOntologyTaskManager manager, Class<? extends Patterns> c, Ontology...ontologies) {
			this.manager = manager;
			this.c = c;
			this.ontologies = ontologies;
		}
		
		List<TermGenerationOutput> run(final Ontology ontology, final List<TermGenerationInput> generationTasks) {
			TermGenerationMultiOntologyTask task = new TermGenerationMultiOntologyTask(c, ontology, generationTasks);
			manager.runManagedTask(task, ontologies);
			if (task.exception != null) {
				throw new RuntimeException(task.exception);
			}
			return task.terms;
		}
		
		private static class TermGenerationMultiOntologyTask extends MultiOntologyTask {

			private final Ontology ontology;
			private final List<TermGenerationInput> generationTasks;
			
			private List<TermGenerationOutput> terms = null;
			private Exception exception = null;
			private final Class<? extends Patterns> c;

			public TermGenerationMultiOntologyTask(Class<? extends Patterns> c, Ontology ontology, List<TermGenerationInput> generationTasks) {
				this.c = c;
				this.ontology = ontology;
				this.generationTasks = generationTasks;
			}
			
			@Override
			public void run(List<OWLGraphWrapper> requested) {
				try {
					Constructor<? extends Patterns> constructor = c.getDeclaredConstructor(List.class);
					Patterns pattern = constructor.newInstance(requested);
					terms = pattern.generateTerms(ontology, generationTasks);
				} catch (Exception exception) {
					this.exception = exception;
					return;
				}
				
			}
			
		}
	}
}