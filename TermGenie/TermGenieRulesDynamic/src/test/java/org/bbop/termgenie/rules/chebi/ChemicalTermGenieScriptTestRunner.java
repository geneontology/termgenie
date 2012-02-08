package org.bbop.termgenie.rules.chebi;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.FileCachingIRIMapper;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlGraphWrapperNameProvider;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;

public class ChemicalTermGenieScriptTestRunner {

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
			bind(IRIMapper.class, FileCachingIRIMapper.class);
			bind("FileCachingIRIMapperLocalCache", new File(FileUtils.getTempDirectory(),"termgenie-download-cache").getAbsolutePath());
			bind("FileCachingIRIMapperPeriod", 6L);
			bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
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

		TemplateField field = termTemplate.getFields().get(0);

		parameters.setTermValues(field.getName(), Arrays.asList("CHEBI:35808")); // Citrate(2-)
//		parameters.setTermValues(field.getName(), Arrays.asList("CHEBI:16947")); // Citrate(3-) has a synonym Citrate, but is not equivalent to Citrate(2-)
		parameters.setStringValues(field.getName(), Arrays.asList("metabolism"));

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(ontology, generationTasks);
		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		System.out.println(output);

		final Frame frame = list.get(0).getTerm();
		OntologyTaskManager taskManager = loader.getOntology(ontology);
		taskManager.runManagedTask(new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws Exception {
				System.out.println("--------");
				System.out.println(OboWriterTools.writeFrame(frame, new OwlGraphWrapperNameProvider(managed)));
				System.out.println("--------");
			}
		});
		assertNotNull(frame);
	}

}
