package org.bbop.termgenie.rules;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;

import owltools.graph.OWLGraphWrapper;

public class HardCodedTermGenerationEngine extends DefaultTermTemplates implements TermGenerationEngine {

	private final Map<String, PatternInstance> patterns;

	public HardCodedTermGenerationEngine(List<OntologyTaskManager> managers) {
		OntologyTaskManager go = null;
		OntologyTaskManager pro = null;
		OntologyTaskManager uberon = null;
		OntologyTaskManager plant = null;
		OntologyTaskManager hpo = null;
		OntologyTaskManager fma = null;
		OntologyTaskManager pato = null;
		OntologyTaskManager cell = null;
		OntologyTaskManager omp = null;
		
		for (OntologyTaskManager manager : managers) {
			if (manager.hasRealOntology()) {
				if (equals(GENE_ONTOLOGY, manager)) {
					go = manager;
				}
				else if (equals(PROTEIN_ONTOLOGY, manager)) {
					pro = manager;
				}
				else if (equals(UBERON_ONTOLOGY, manager)) {
					uberon = manager;
				}
				else if (equals(PLANT_ONTOLOGY, manager)) {
					plant = manager;
				}
				else if (equals(HP_ONTOLOGY, manager)) {
					hpo = manager;
				}
				else if (equals(FMA_ONTOLOGY, manager)) {
					fma = manager;
				}
				else if (equals(PATO, manager)) {
					pato = manager;
				}
				else if (equals(CELL_ONTOLOGY, manager)) {
					cell = manager;
				}
				else if (equals(OMP, manager)) {
					omp = manager;
				}
			}
		}
		patterns = new HashMap<String, PatternInstance>();
		patterns.put(GENE_ONTOLOGY.getUniqueName(), new PatternInstance(GeneOntologyComplexPatterns.class, go, pro, uberon, plant));
		patterns.put(HP_ONTOLOGY.getUniqueName(), new PatternInstance(HumanPhenotypePatterns.class, hpo, fma, pato));
		patterns.put(OMP.getUniqueName(), new PatternInstance(MicrobialPhenotypePatterns.class, omp, go, pato));
		patterns.put(CELL_ONTOLOGY.getUniqueName(), new PatternInstance(CellOntologyPatterns.class, cell, uberon, pro, go));
		patterns.put(UBERON_ONTOLOGY.getUniqueName(), new PatternInstance(UberonPatterns.class, uberon));
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
			List<TermGenerationOutput> terms = instance.getInstance().generateTerms(ontology, generationTasks);
			return terms;
		}
		// TODO decide if to set error message for unknown ontology
		return null;	
	}
	
	private static boolean equals(Ontology o1, OntologyTaskManager manager) {
		Ontology o2 = manager.getOntology();
		if (o1 == o2) {
			return true;
		}
		return o1.getUniqueName().equals(o2.getUniqueName());
	}
	
	private static class PatternInstance {
		// TODO this is realy ugly: reimplement with the option to use multiple Managers at once. 
		// Try to avoid a a possible deadlock, when competing for multiple resources! 
		// Here: multiple ontology task managers
		
		private final Class<? extends Patterns> c;
		private final OntologyTaskManager[] managers;

		PatternInstance(Class<? extends Patterns> c, OntologyTaskManager...managers) {
			this.c = c;
			this.managers = managers;
		}
		
		private static OWLGraphWrapper[] getOwlGraphWrapper(OntologyTaskManager...managers) {
			OWLGraphWrapper[] wrappers = new OWLGraphWrapper[managers.length];
			InstanceRetriever retriever = new InstanceRetriever();
			for (int i=0; i<managers.length;i++) {
				managers[i].runManagedTask(retriever);
				wrappers[i] = retriever.instance;
			}
			return wrappers;
		}
		
		private static class InstanceRetriever implements OntologyTask {
			private OWLGraphWrapper instance = null;

			@Override
			public void run(OWLGraphWrapper managed) {
				instance = managed;
			}
		}

		Patterns getInstance() {
			try {
				Constructor<? extends Patterns> constructor = c.getDeclaredConstructor(OWLGraphWrapper[].class);
				Object[] args = new Object[1];
				args[0] = getOwlGraphWrapper(managers);
				return constructor.newInstance(args);
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
		
	}
}