package org.bbop.termgenie.index;

import static org.junit.Assert.*;

import java.util.Collection;

import org.bbop.termgenie.core.TermSuggestion;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class LuceneMemoryOntologyIndexTest {

	@Test
	public void testLuceneMemoryOntologyIndex() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule(),
				new ReasonerModule(null));
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		ConfiguredOntology go = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontology = injector.getInstance(OntologyLoader.class).getOntology(go);
		final ReasonerFactory factory = injector.getInstance(ReasonerFactory.class);

		OntologyTask task = new OntologyTask() {

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws Exception {
				LuceneMemoryOntologyIndex index = new LuceneMemoryOntologyIndex(managed, null, null, null, factory);
				Collection<SearchResult> results = index.search(" me  pigmentation ", 5, null);
				for (SearchResult searchResult : results) {
					TermSuggestion suggestion = searchResult.term;
					String id = suggestion.getIdentifier();
					assertNotNull(suggestion.getLabel());
					assertNotNull(suggestion.getDescription());
					OWLObject owlObject = managed.getOWLObjectByIdentifier(id);
					String label = managed.getLabel(owlObject);
					System.out.println(id + "  " + searchResult.score + "  " + label);
				}
				assertEquals(2, results.size());
			}
		};
		ontology.runManagedTask(task);
		if (task.getException() != null) {
			throw new RuntimeException(task.getException());
		}
	}

}
