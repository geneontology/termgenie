package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.CatalogXmlIRIMapper;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CcTransportFromToTest {

	public static final class ChemicalTestOntologyModule extends XMLReloadingOntologyModule {

		public ChemicalTestOntologyModule() {
			super("ontology-configuration_chemical.xml", null, null);
		}

		@Override
		protected void bindIRIMapper() {
			// do nothing, use @Provides instead
		}
	
		@Singleton
		@Provides
		protected IRIMapper provideIRIMapper() {
			String catalogXml = "src/test/resources/ontologies/catalog-v001.xml";
			return new CatalogXmlIRIMapper(null, catalogXml);
		}
	}
	
	private static TermGenerationEngine generationEngine;
	private static OntologyConfiguration configuration;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_other.xml", false, null),
				new ChemicalTestOntologyModule(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		configuration = injector.getInstance(OntologyConfiguration.class);
	}

	@Test
	public void test_cc_transport_from_to() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();
		
		TemplateField field0 = termTemplate.getFields().get(0);
		TemplateField field1 = termTemplate.getFields().get(1);
		String fieldName0 = field0.getName();
		String fieldName1 = field1.getName();

		// this is a non-sense example, cell surface should not be used as a target
		parameters.setTermValues(fieldName0, Arrays.asList("GO:0005791")); // rough endoplasmic reticulum
		parameters.setTermValues(fieldName1, Arrays.asList("GO:0009986")); // cell surface
		
		parameters.setStringValues(fieldName0, Arrays.asList("transport", "vesicle-mediated transport"));
		
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks, false, null);

		assertNotNull(list);
		assertEquals(2, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertNull(output1.getError(), output1.getError());
		
		TermGenerationOutput output2 = list.get(1);
		assertNull(output2.getError(), output2.getError());
		
		Frame term1 = output1.getTerm();
		
		Frame term2 = output2.getTerm();
		Collection<Clause> isaClauses = term2.getClauses(OboFormatTag.TAG_IS_A);
		boolean found = false;
		for (Clause clause : isaClauses) {
			if (term1.getId().equals(clause.getValue())) {
				found = true;
			}
		}
		assertTrue("The second term should be a direct subclass of the first one.", found);
	}
	
	@Test
	public void test_cc_transport() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(1);
		TermGenerationParameters parameters = new TermGenerationParameters();
		
		TemplateField field0 = termTemplate.getFields().get(0);
		String fieldName0 = field0.getName();

		// this is a non-sense example
		parameters.setTermValues(fieldName0, Arrays.asList("GO:0005791")); // rough endoplasmic reticulum
		parameters.setStringValues(fieldName0, Arrays.asList("transport", "vesicle-mediated transport"));
		
		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks, false, null);

		assertNotNull(list);
		assertEquals(2, list.size());
		TermGenerationOutput output1 = list.get(0);
		assertNull(output1.getError(), output1.getError());
		
		TermGenerationOutput output2 = list.get(1);
		assertNull(output2.getError(), output2.getError());
		
		Frame term1 = output1.getTerm();
		
		Frame term2 = output2.getTerm();
		Collection<Clause> isaClauses = term2.getClauses(OboFormatTag.TAG_IS_A);
		boolean found = false;
		for (Clause clause : isaClauses) {
			if (term1.getId().equals(clause.getValue())) {
				found = true;
			}
		}
		assertTrue("The second term should be a direct subclass of the first one.", found);
		System.out.println(term2);
	}

}
