package org.bbop.termgenie.solr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration.ConfiguredOntology;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import owltools.graph.OWLGraphWrapper;

public class PlantOntologyIndexTest {

	protected static OntologyTaskManager plantManager;
	protected static Ontology plant;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = Guice.createInjector(new DefaultOntologyModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		Map<String, ConfiguredOntology> ontologies = configuration.getOntologyConfigurations();
		OntologyLoader loader = injector.getInstance(OntologyLoader.class);
		plant = ontologies.get("PO");
		ConfiguredOntology ontology = ontologies.get("PO");
		plantManager = loader.getOntology(ontology);
	}
	
	@Test
	public void testPlantOntologyIndex() {
		plantManager.runManagedTask(new OntologyTask() {

			@Override
			public boolean run(OWLGraphWrapper managed) {
				try {
					LuceneMemoryOntologyIndex index = createIndex(managed);
					index.search("trunk", 10, null);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
				return false;
			}
		});
	}

	protected LuceneMemoryOntologyIndex createIndex(OWLGraphWrapper ontology) throws IOException {
		List<String> roots = plant.getRoots();
		List<Pair<String, List<String>>> branches = null;
		LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(ontology, roots, branches) {

			@Override
			protected ReasonerTaskManager getReasonerManager(OWLGraphWrapper ontology) {
				return ReasonerFactory.getTaskManager(ontology, "jcel");
			}
			
		};
		return index;
	}
}
