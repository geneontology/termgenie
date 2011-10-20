package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.AbstractOntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
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
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Injector;

public class TermGenieScriptTestRunner {

	private static TermGenerationEngine generationEngine;
	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_simple.xml"),
				new DefaultOntologyModule() {

					@Override
					protected void bindOntologyConfiguration() {
						bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
						bind("XMLOntologyConfigurationResource",
								"ontology-configuration_simple.xml");
					}
				},
				new ReasonerModule("hermit"));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters(termTemplate.getFieldCount());

		OntologyTaskManager ontologyManager = loader.getOntology(ontology);

		parameters.setTermValues(termTemplate, 0, getTerm("GO:0043473", ontologyManager));
		parameters.setStringValues(termTemplate,
				0,
				"regulation",
				"negative_regulation",
				"positive_regulation");

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);

		assertNotNull(list);
		assertEquals(3, list.size());

		OntologyTerm<ISynonym, IRelation> term1 = list.get(0).getTerm();
		assertEquals("regulation of pigmentation", term1.getLabel());

		OntologyTerm<ISynonym, IRelation> term2 = list.get(1).getTerm();
		assertEquals("negative regulation of pigmentation", term2.getLabel());
		List<IRelation> relations = term2.getRelations();
		boolean found = false;
		for (IRelation relation : relations) {
			if (relation.getTarget().equals(term1.getId())) {
				assertEquals(term1.getLabel(), relation.getTargetLabel());
				found = true;
			}
		}
		assertEquals("positive regulation of pigmentation", list.get(2).getTerm().getLabel());
		assertTrue(found);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(1);
		TermGenerationParameters parameters = new TermGenerationParameters(termTemplate.getFieldCount());

		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		parameters.setTermValues(termTemplate, 0, getTerm("GO:0046836", ontologyManager)); // glycolipid
																							// transport
		parameters.setTermValues(termTemplate, 1, getTerm("GO:0006915", ontologyManager)); // apoptosis

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		OntologyTerm<ISynonym, IRelation> term = output.getTerm();

		List<Clause> clauses = OBOConverterTools.translateRelations(term.getRelations(), null);
		assertEquals(4, clauses.size());
		Clause clause0 = clauses.get(0);
		assertEquals(OboFormatTag.TAG_IS_A.getTag(), clause0.getTag());
		assertEquals("GO:0046836", clause0.getValue());

		Clause clause1 = clauses.get(1);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause1.getTag());
		assertEquals("GO:0046836", clause1.getValue());

		Clause clause2 = clauses.get(2);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause2.getTag());
		assertEquals("part_of", clause2.getValue());
		assertEquals("GO:0006915", clause2.getValue2());

		Clause clause3 = clauses.get(3);
		assertEquals(OboFormatTag.TAG_RELATIONSHIP.getTag(), clause3.getTag());
		assertEquals("part_of", clause3.getValue());
		assertEquals("GO:0006915", clause3.getValue2());
	}

	private OntologyTerm<ISynonym, IRelation> getTerm(String id, OntologyTaskManager ontologyManager)
	{
		OntologyTaskImplementation task = new OntologyTaskImplementation(id);
		ontologyManager.runManagedTask(task);
		return task.term;
	}

	private final class OntologyTaskImplementation extends OntologyTask {

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
		protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
			OWLObject x = managed.getOWLObjectByIdentifier(id);
			String label = managed.getLabel(x);
			String definition = managed.getDef(x);
			List<ISynonym> synonyms = managed.getOBOSynonyms(x);
			List<String> defXRef = managed.getDefXref(x);
			Map<String, String> metaData = new HashMap<String, String>();
			List<IRelation> relations = Collections.emptyList();
			term = new DefaultOntologyTerm(id, label, definition, synonyms, defXRef, metaData, relations);
		}
	}
}
