package org.bbop.termgenie.solr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class PlantOntologyIndexTest {

	protected static OntologyTaskManager plantManager;
	protected static Ontology plant;
	protected static ReasonerFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule(),
				new ReasonerModule(null));
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		Map<String, ConfiguredOntology> ontologies = configuration.getOntologyConfigurations();
		OntologyLoader loader = injector.getInstance(OntologyLoader.class);
		plant = ontologies.get("PO");
		ConfiguredOntology ontology = ontologies.get("PO");
		plantManager = loader.getOntology(ontology);
		factory = injector.getInstance(ReasonerFactory.class);
	}

	@Test
	public void testPlantOntologyIndex() throws Exception {
		OntologyTask task = new OntologyTask() {

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws Exception {
				LuceneMemoryOntologyIndex index = createIndex(managed);
				index.search("trunk", 10, null);
			}
		};
		plantManager.runManagedTask(task);
		if (task.getException() != null) {
			throw new RuntimeException(task.getException());
		}
	}

	protected LuceneMemoryOntologyIndex createIndex(OWLGraphWrapper ontology) throws IOException {
		List<String> roots = plant.getRoots();
		LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(ontology, roots, null, null, factory)
		{

			@Override
			protected ReasonerTaskManager getReasonerManager(OWLGraphWrapper ontology,
					ReasonerFactory reasonerFactory)
			{
				return reasonerFactory.getTaskManager(ontology, "jcel");
			}

		};
		return index;
	}
}
