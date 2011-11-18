package org.bbop.termgenie.ontology.obo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModuleTest.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Injector;


public class OwlTranslatorToolsTest {
	
	private static OntologyConfiguration configuration;
	private static OntologyLoader loader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule() {

			@Override
			protected void bindOntologyConfiguration() {
				bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
				bind("XMLOntologyConfigurationResource", "ontology-configuration_simple_go.xml");
			}
		});

		configuration = injector.getInstance(OntologyConfiguration.class);
		loader = injector.getInstance(OntologyLoader.class);
	}

	@Test
	public void testExtractRelations() throws Exception {
		ConfiguredOntology ontology = configuration.getOntologyConfigurations().get("GeneOntology");
		OntologyTaskManager ontologyManager = loader.getOntology(ontology);
		OntologyTask task = new OntologyTask() {

			@Override
			protected void runCatching(final OWLGraphWrapper wrapper)
					throws TaskException, Exception
			{
					Owl2Obo owl2Obo = new Owl2Obo();
					OBODoc oboDoc = owl2Obo.convert(wrapper.getSourceOntology());
					Set<OWLObject> allOWLObjects = wrapper.getAllOWLObjects();
					for (OWLObject owlObject : allOWLObjects) {
						if (owlObject instanceof OWLClass) {
							OWLClass owlClass = (OWLClass) owlObject;
							List<Clause> relations = OwlTranslatorTools.extractRelations(owlClass, wrapper);
							List<Clause> oboRelations = getRelations(owlClass, oboDoc);
							compareRelations(oboRelations, relations, owlClass);
						}
					}
			}
		};
		ontologyManager.runManagedTask(task);
		if (task.getException() != null) {
			throw task.getException();
		}
	}
	
	private List<Clause> getRelations(OWLClass owlClass, OBODoc oboDoc) {
		String frameId = Owl2Obo.getIdentifier(owlClass.getIRI());
		Frame frame = oboDoc.getTermFrame(frameId);
		if (frame == null) {
			// happens for alt_id
			return Collections.emptyList();
		}
		List<Clause> relations = OboTools.getRelations(frame);
		return relations;
	}
	
	
	private void compareRelations(List<Clause> oboRelations, List<Clause> relations, OWLClass owlClass) {
		List<Clause> obo = new ArrayList<Clause>();
		List<Clause> rel = new ArrayList<Clause>();
		if (oboRelations != null) {
			for (Clause relation : oboRelations) {
				if (!OboFormatTag.TAG_DISJOINT_FROM.getTag().equals(relation.getTag())) {
					obo.add(relation);
				}
			}
		}
		if (relations != null) {
			for (Clause relation : relations) {
				if (!OboFormatTag.TAG_DISJOINT_FROM.getTag().equals(relation.getTag())) {
					rel.add(relation);
				}
			}
		}
		assertEquals("Relation extraction failed for: "+Owl2Obo.getIdentifier(owlClass.getIRI()),
				obo.size(), rel.size());
		
		List<Clause> missing = new ArrayList<Clause>();
		for (Clause oboRelation : obo) {
			boolean found = false;
			for (Clause relation : rel) {
				if(oboRelation.equals(relation)) {
					found = true;
				}
			}
			if (!found) {
				missing.add(oboRelation);
			}
		}
		if (!missing.isEmpty()) {
			fail("Missing relations:\n"+missing);
		}
	}
}
