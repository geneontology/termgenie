package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.junit.Test;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class DefaultOntologyModuleTest {

	public static class TestDefaultOntologyModule extends DefaultOntologyModule {

		public TestDefaultOntologyModule() {
			super(null);
		}

		@Override
		protected void bindIRIMapper() {
			bind(IRIMapper.class, LocalFileIRIMapper.class);
			bind("LocalFileIRIMapperResource", LocalFileIRIMapper.SETTINGS_FILE);
		}
	}

	@Test
	public void testDefaultOntologyModuleStringString() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule());
		OntologyConfiguration configuration = injector.getInstance(OntologyConfiguration.class);
		MultiOntologyTaskManager manager = injector.getInstance(MultiOntologyTaskManager.class);
		MultiOntologyTask task = new MultiOntologyTask() {

			@Override
			public List<Modified> run(List<OWLGraphWrapper> requested) {
				assertTrue(requested.size() > 0);
				return null;
			}
		};
		manager.runManagedTask(task, configuration.getOntologyConfigurations().get("GeneOntology"));
	}

}
