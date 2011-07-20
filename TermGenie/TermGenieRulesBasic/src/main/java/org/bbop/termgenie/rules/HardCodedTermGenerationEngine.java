package org.bbop.termgenie.rules;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HardCodedTermGenerationEngine implements TermGenerationEngine {

	private final Map<String, PatternInstance> patterns;
	private final DefaultTermTemplates templates;

	@Inject
	HardCodedTermGenerationEngine(MultiOntologyTaskManager manager, DefaultTermTemplates templates) {
		this.templates = templates;
		patterns = new HashMap<String, PatternInstance>();
		patterns.put(templates.GENE_ONTOLOGY.getUniqueName(),
				new PatternInstance(manager,
					GeneOntologyComplexPatterns.class, 
					templates,
					templates.GENE_ONTOLOGY, 
					templates.PROTEIN_ONTOLOGY, 
					templates.UBERON_ONTOLOGY, 
					templates.PLANT_ONTOLOGY));
		patterns.put(templates.HP_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					HumanPhenotypePatterns.class,
					templates,
					templates.HP_ONTOLOGY, 
					templates.FMA_ONTOLOGY, 
					templates.PATO));
		patterns.put(templates.OMP.getUniqueName(), 
				new PatternInstance(manager,
					MicrobialPhenotypePatterns.class,
					templates,
					templates.OMP, 
					templates.GENE_ONTOLOGY, 
					templates.PATO));
		patterns.put(templates.CELL_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					CellOntologyPatterns.class,
					templates,
					templates.CELL_ONTOLOGY, 
					templates.UBERON_ONTOLOGY, 
					templates.PROTEIN_ONTOLOGY, 
					templates.GENE_ONTOLOGY));
		patterns.put(templates.UBERON_ONTOLOGY.getUniqueName(), 
				new PatternInstance(manager,
					UberonPatterns.class,
					templates,
					templates.UBERON_ONTOLOGY));
	}
	
	@Override
	public List<TermTemplate> getAvailableTemplates() {
		return templates.defaultTemplates;
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
		private final DefaultTermTemplates templates;

		PatternInstance(MultiOntologyTaskManager manager, Class<? extends Patterns> c, DefaultTermTemplates templates, Ontology...ontologies) {
			this.manager = manager;
			this.c = c;
			this.templates = templates;
			this.ontologies = ontologies;
		}
		
		List<TermGenerationOutput> run(final Ontology ontology, final List<TermGenerationInput> generationTasks) {
			TermGenerationMultiOntologyTask task = new TermGenerationMultiOntologyTask(c, templates, ontology, generationTasks);
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
			private final DefaultTermTemplates templates;

			public TermGenerationMultiOntologyTask(Class<? extends Patterns> c, DefaultTermTemplates templates, Ontology ontology, List<TermGenerationInput> generationTasks) {
				this.c = c;
				this.ontology = ontology;
				this.generationTasks = generationTasks;
				this.templates = templates;
			}
			
			@Override
			public void run(List<OWLGraphWrapper> requested) {
				try {
					Constructor<? extends Patterns> constructor = c.getDeclaredConstructor(List.class, DefaultTermTemplates.class);
					Patterns pattern = constructor.newInstance(requested, templates);
					terms = pattern.generateTerms(ontology, generationTasks);
				} catch (Exception exception) {
					this.exception = exception;
					return;
				}
				
			}
			
		}
	}
}