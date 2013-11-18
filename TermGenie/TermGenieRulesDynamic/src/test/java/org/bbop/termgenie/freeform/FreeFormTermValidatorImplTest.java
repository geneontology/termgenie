package org.bbop.termgenie.freeform;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerFactoryImpl;
import org.bbop.termgenie.freeform.FreeFormTermRequest.Xref;
import org.bbop.termgenie.freeform.FreeFormTermValidatorImpl.ValidationTask;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.CatalogXmlIRIMapper;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.TemporaryIdentifierTools;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;


/**
 * Tests for {@link FreeFormTermValidatorImpl}.
 */
public class FreeFormTermValidatorImplTest {
	
	private static ReasonerFactory factory = null;
	private static OWLGraphWrapper graph = null;

	
	static final class FreeFormTestOntologyModule extends XMLReloadingOntologyModule {

		FreeFormTestOntologyModule() {
			super("ontology-configuration_freeform.xml", 
					Arrays.asList("http://purl.obolibrary.org/obo/go.owl", "http://purl.obolibrary.org/obo/go/extensions/x-chemical.owl"), null);
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
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new FreeFormTestOntologyModule());

		OntologyLoader loader = injector.getInstance(OntologyLoader.class);
		ConfiguredOntology go = injector.getInstance(OntologyConfiguration.class).getOntologyConfigurations().get("GeneOntology");
		
		OntologyTaskManager goManager = loader.getOntology(go);
		
		goManager.runManagedTask(new OntologyTask(){

			@Override
			protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception
			{
				graph = managed;
			}
		});
		
		factory = new ReasonerFactoryImpl();
	}
	
	@Test
	public void testValidateMinimalRules() throws Exception {
		
		// validate empty and partially empty requests
		// empty
		errors(new TestFreeFormTermRequest(), "label", graph);

		final TestFreeFormTermRequest request = new TestFreeFormTermRequest();
		request.setLabel("too short");
		errors(request, "label", graph);

		// exact synonym for GO:0060444 ! branching involved in mammary gland duct morphogenesis
		request.setLabel("mammary gland branching morphogenesis");
		errors(request, "label", graph);

		request.setLabel("not too short fake label");
		errors(request, "namespace", graph);

		request.setNamespace("");
		errors(request, "namespace", graph);

		request.setNamespace("biological_process");
		errors(request, "parents", graph);

		request.setIsA(Collections.singletonList("GO:0009058"));
		errors(request, "definition", graph);

		request.setDefinition("Term definition for fake term with non ASCII char: 3′-5′");
		errors(request, "definition", graph);

		request.setDefinition("Term definition for fake term.");
		errors(request, "definition db xref", graph);

		request.setDbxrefs(Arrays.asList("GOC:fake"));
		errors(request, "definition db xref", graph);

		request.setDbxrefs(Arrays.asList("GOC:fake", "PMID:0000001"));

		Xref xref = new Xref();
		xref.setIdRef("FAKE:fake");
		xref.setAnnotation("Fake comment");
		request.setXrefs(Collections.<Xref>singletonList(xref));

		Pair<Frame,Set<OWLAxiom>> pair = noErrors(request, graph);

		assertNotNull(pair.getOne());
		assertNotNull(pair.getTwo());
	
	}
	
	@Test
	public void testDisjoints() throws Exception {
		final TestFreeFormTermRequest request = new TestFreeFormTermRequest();
		request.setLabel("H-NS complex fake test 2");
		
		request.setNamespace("cellular_component");
		request.setIsA(Arrays.asList("GO:0044445", "GO:0005667"));
		request.setDefinition("A protein-DNA complex involved in bacterial nucleoid condensation and negative regulation of global gene expression by directly binding to promoter regions. Recognizes both structural and sequence-specific motifs in double-stranded DNA and has binding preference for bent DNA.");
		request.setDbxrefs(Arrays.asList("GOC:fake", "PMID:000000"));
		
		errors(request, "relations", graph);
	}
	
	@Test
	public void testHasPart() throws Exception {
		final TestFreeFormTermRequest request = new TestFreeFormTermRequest();
		request.setLabel("bla bla bla bla bla bla test");
		
		request.setNamespace("biological_process");
		request.setIsA(Arrays.asList("GO:0043473"));
		request.setHasPart(Arrays.asList("GO:0048069", "GO:0048592"));
		request.setDefinition("Foo bar bla bla bla bla bla bla bla bla bla bla.");
		request.setDbxrefs(Arrays.asList("GOC:fake", "PMID:000000"));
		
		Pair<Frame,Set<OWLAxiom>> pair = noErrors(request, graph);

		Frame frame = pair.getOne();
		
		assertNotNull(frame);
		assertNotNull(pair.getTwo());
	}
	
	@Test
	public void testNonAscii() throws Exception {
		assertEquals(0, ValidationTask.hasNonAscii("Bla bla bla.").size());
		assertEquals(1, ValidationTask.hasNonAscii("3′-5′").size());
	}
	
	private void errors(FreeFormTermRequest request, String field, OWLGraphWrapper graph) {
		ValidationTask task = createTask(request, graph);
		task.runInternal(graph);
		List<FreeFormHint> errors = task.errors;
		if (errors != null && !errors.isEmpty()) {
			for (FreeFormHint freeFormHint : errors) {
				assertEquals(field, freeFormHint.getField());
			}
			
		}
		else {
			fail("Errors expected.");
		}
	}

	protected ValidationTask createTask(FreeFormTermRequest request, OWLGraphWrapper graph) {
		String idprefix = TemporaryIdentifierTools.getTempIdPrefix(graph);
		List<String> namespaces = Arrays.asList("biological_process");
		ValidationTask task = new FreeFormTermValidatorImpl.ValidationTask(request, true, true, true, "freeform", idprefix, namespaces , factory, ProcessState.NO);
		return task;
	}
	
	private Pair<Frame,Set<OWLAxiom>> noErrors(FreeFormTermRequest request, OWLGraphWrapper graph) {
		ValidationTask task = createTask(request, graph);
		task.runInternal(graph);
		List<FreeFormHint> errors = task.errors;
		if (errors != null && !errors.isEmpty()) {
			for (FreeFormHint freeFormHint : errors) {
				System.err.println("Error: Field: "+freeFormHint.getField()+" Message: "+freeFormHint.getHint());
			}
			fail("No errors expected.");
		}
		return task.term;
	}
	
	private static class TestFreeFormTermRequest implements FreeFormTermRequest {

		private String label;
		private String namespace;

		private String definition;
		private List<String> dbxrefs;

		private List<String> isA;
		private List<String> partOf;
		private List<String> hasPart;
		
		private List<Xref> xrefs;
		
		@Override
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		@Override
		public String getDefinition() {
			return definition;
		}

		public void setDefinition(String definition) {
			this.definition = definition;
		}

		@Override
		public List<String> getDbxrefs() {
			return dbxrefs;
		}

		public void setDbxrefs(List<String> dbxrefs) {
			this.dbxrefs = dbxrefs;
		}

		@Override
		public List<String> getIsA() {
			return isA;
		}

		public void setIsA(List<String> isA) {
			this.isA = isA;
		}

		@Override
		public List<String> getPartOf() {
			return partOf;
		}

		@SuppressWarnings("unused")
		public void setPartOf(List<String> partOf) {
			this.partOf = partOf;
		}

		@Override
		public List<String> getHasPart() {
			return hasPart;
		}
		
		public void setHasPart(List<String> hasPart) {
			this.hasPart = hasPart;
		}

		@Override
		public List<? extends ISynonym> getISynonyms() {
			// TODO Auto-generated method stub
			return null;
		}
		
		/**
		 * @param xrefs the xrefs to set
		 */
		public void setXrefs(List<Xref> xrefs) {
			this.xrefs = xrefs;
		}

		@Override
		public List<Xref> getXrefs() {
			return xrefs;
		}

		@Override
		public List<String> getCapableOf() {
			return null;
		}
		
	}

}
