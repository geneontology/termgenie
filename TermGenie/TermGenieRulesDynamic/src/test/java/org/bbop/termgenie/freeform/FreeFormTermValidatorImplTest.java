package org.bbop.termgenie.freeform;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerFactoryImpl;
import org.bbop.termgenie.freeform.FreeFormTermValidatorImpl.ValidationTask;
import org.bbop.termgenie.rules.TemporaryIdentifierTools;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.io.ParserWrapper;


/**
 * Tests for {@link FreeFormTermValidatorImpl}.
 */
public class FreeFormTermValidatorImplTest {
	
	private static OWLGraphWrapper graph = null;
	private static ReasonerFactory factory = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		ParserWrapper pw = new ParserWrapper();
		
		IRI iri = IRI.create((new File("src/test/resources/ontologies/gene_ontology_write.obo")).getCanonicalFile());
		graph = pw.parseToOWLGraph(iri.toString());
		factory = new ReasonerFactoryImpl();
	}
	
	@Test
	public void testValidateMinimalRules() {
		
		// validate empty and partially empty requests
		// empty
		errors(new TestFreeFormTermRequest(), "label");
		
		final TestFreeFormTermRequest request = new TestFreeFormTermRequest();
		request.setLabel("too short");
		errors(request, "label");
		
		// biosynthesis is a synonym for GO:0009058 biosynthetic process
		request.setLabel("biosynthesis");
		errors(request, "label");
		
		request.setLabel("not too short fake label");
		errors(request, "namespace");
		
		request.setNamespace("");
		errors(request, "namespace");
		
		request.setNamespace("biological_process");
		errors(request, "parents");
		
		request.setIsA(Collections.singletonList("GO:0009058"));
		errors(request, "definition");
		
		request.setDefinition("Term definition for fake term.");
		errors(request, "definition db xref");
		
		request.setDbxrefs(Arrays.asList("GOC:fake"));
		errors(request, "definition db xref");
		
		request.setDbxrefs(Arrays.asList("GOC:fake", "PMID:0000001"));
		
		Pair<Frame,Set<OWLAxiom>> pair = noErrors(request);
		
		assertNotNull(pair.getOne());
		assertNotNull(pair.getTwo());
	}
	
	private void errors(FreeFormTermRequest request, String field) {
		ValidationTask task = createTask(request);
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

	protected ValidationTask createTask(FreeFormTermRequest request) {
		String idprefix = TemporaryIdentifierTools.getTempIdPrefix(graph);
		ValidationTask task = new FreeFormTermValidatorImpl.ValidationTask(request, true, true, "freeform", idprefix, factory, ProcessState.NO);
		return task;
	}
	
	private Pair<Frame,Set<OWLAxiom>> noErrors(FreeFormTermRequest request) {
		ValidationTask task = createTask(request);
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
		public List<? extends ISynonym> getISynonyms() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
