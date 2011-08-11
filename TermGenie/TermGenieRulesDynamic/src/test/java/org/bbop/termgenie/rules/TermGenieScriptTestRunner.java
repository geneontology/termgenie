package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.OntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

import com.google.inject.Injector;

public class TermGenieScriptTestRunner {

	private static TermGenerationEngine generationEngine;
	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new DefaultDynamicRulesModule(), new DefaultOntologyModule(), new ReasonerModule());
		
		generationEngine = injector.getInstance(TermGenerationEngine.class);
		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}
	
	@Test
	public void test1() {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters(termTemplate.getFieldCount());
		
		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		
		parameters.setTermValues(termTemplate, 0, getTerm("GO:0043473", ontologyManager));
		parameters.setStringValues(termTemplate, 0, "regulation", "negative_regulation", "positive_regulation");
		
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters );
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);
		
		assertNotNull(list);
		assertEquals(3, list.size());
		
		assertEquals("regulation of pigmentation", list.get(0).getTerm().getLabel());
		assertEquals("negative regulation of pigmentation", list.get(1).getTerm().getLabel());
		assertEquals("positive regulation of pigmentation", list.get(2).getTerm().getLabel());
	}
	
	
	private OntologyTerm getTerm(String id, OntologyTaskManager ontologyManager) {
		OntologyTaskImplementation task = new OntologyTaskImplementation(id);
		ontologyManager.runManagedTask(task);
		return task.term;
	}

	private final class OntologyTaskImplementation implements OntologyTask {
		private DefaultOntologyTerm term = null;
		private final String id;
	
		/**
		 * @param id
		 */
		OntologyTaskImplementation(String id) {
			super();
			this.id = id;
		}

		@Override
		public boolean run(OWLGraphWrapper managed) {
			OWLObject x = managed.getOWLObjectByIdentifier(id);
			String label = managed.getLabel(x);
			String definition = managed.getDef(x);
			List<Synonym> synonyms = managed.getOBOSynonyms(x);
			List<String> defXRef = managed.getDefXref(x);
			Map<String, String> metaData = new HashMap<String, String>();
			List<Relation> relations = Collections.emptyList();
			term = new DefaultOntologyTerm(id, label, definition, synonyms, defXRef, metaData, relations);
			return false;
		}
	}
}
