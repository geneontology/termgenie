package org.bbop.termgenie.rules.chebi;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.CatalogXmlIRIMapper;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class ChemicalTermGenieScriptCatalogXmlTestRunner {

	static final class ChemicalTestOntologyModule extends DefaultOntologyModule {

		ChemicalTestOntologyModule() {
			super(null);
		}

		@Override
		protected void bindOntologyConfiguration() {
			bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
			bind("XMLOntologyConfigurationResource",
					"ontology-configuration_chemical.xml");
		}

		@Override
		protected void bindIRIMapper() {
			bind(IRIMapper.class, "FallbackIRIMapper", NoExternalMapper.class);
		}
		
		@Provides
		@Singleton
		public IRIMapper getDefaultIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallBackIRIMapper) {
			File homeDir = FileUtils.getUserDirectory();
			String catalogXml = homeDir.getAbsolutePath()+"/svn/committer/go-trunk/ontology/extensions/catalog-v001.xml";
			return new CatalogXmlIRIMapper(fallBackIRIMapper, catalogXml);
		}
	}

	public static final class NoExternalMapper implements IRIMapper {

		@Override
		public IRI getDocumentIRI(IRI ontologyIRI) {
			fail("No external imports allowed: "+ontologyIRI);
			throw new RuntimeException("No external imports allowed: "+ontologyIRI);
		}

		@Override
		public URL mapUrl(String url) {
			fail("No external imports allowed: "+url);
			throw new RuntimeException("No external imports allowed: "+url);
		}
		
	}
	
	private static TermGenerationEngine generationEngine;
	private static OntologyLoader loader;
	private static OntologyConfiguration configuration;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_chemical.xml", null),
				new ChemicalTestOntologyModule(),
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}

	@Test
	public void test_metabolism_catabolism_biosynthesis() {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(0);
		TermGenerationParameters parameters = new TermGenerationParameters();

		OntologyTaskManager taskManager = loader.getOntology(ontology);
		taskManager.runManagedTask(new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws Exception {
				List<OWLOntology> supports = new ArrayList<OWLOntology>(managed.getSupportOntologySet());
				System.out.println("--------Supports--------");
				for (OWLOntology owlOntology : supports) {
					System.out.println(owlOntology.getOntologyID());
				}
				System.out.println("--------");
				System.out.println("--------Import Closure--------");
				List<OWLOntology> importsClosure = new ArrayList<OWLOntology>(managed.getSourceOntology().getImportsClosure());
				for (OWLOntology owlOntology : importsClosure) {
					System.out.println(owlOntology.getOntologyID());
				}
				System.out.println("--------");
			}
		});
		
		TemplateField field = termTemplate.getFields().get(0);

		parameters.setTermValues(field.getName(), Arrays.asList("UCHEBI:30769")); // citric acid or conjugate
		parameters.setStringValues(field.getName(), Arrays.asList("metabolism"));

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks, null);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		
		assertFalse(output.isSuccess());
		assertEquals("The term GO:0006101 'citrate metabolic process' with the same logic definition already exists", output.getMessage());
	}

}
