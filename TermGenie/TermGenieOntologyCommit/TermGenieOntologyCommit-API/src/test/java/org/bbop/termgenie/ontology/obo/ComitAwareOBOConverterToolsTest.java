package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.handleTerm;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.AbstractOntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.obo.ComitAwareOBOConverterTools.LoadState;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.Test;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;

import owltools.graph.OWLGraphWrapper.Synonym;


public class ComitAwareOBOConverterToolsTest extends ResourceLoader {

	public ComitAwareOBOConverterToolsTest() {
		super(false);
	}

	@Test
	public void testHandleTerm() throws IOException {
		Map<String, String> properties = new HashMap<String, String>();
		Relation.setType(properties, OboFormatTag.TAG_IS_A);
		List<IRelation> relations = Collections.<IRelation>singletonList(new Relation("CARO:0001002", "CARO:0001001", null, properties));
		DefaultOntologyTerm term = new DefaultOntologyTerm("CARO:0001002", "test label 1", null, null, null, null, relations);
		InputStream inputStream = loadResource("caro-mini-test.obo");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		OBOFormatParser parser = new OBOFormatParser();
		OBODoc obodoc = parser.parse(reader);
		assertEquals(LoadState.addSuccess, handleTerm(term, Modification.add, obodoc));
		assertEquals(LoadState.addRedundant, handleTerm(term, Modification.add, obodoc));
		assertClauseCount(2, "CARO:0001002", obodoc);
		assertEquals(LoadState.modifyRedundant, handleTerm(term, Modification.modify, obodoc));
		assertClauseCount(2, "CARO:0001002", obodoc);
		
		List<Synonym> synonyms = Collections.singletonList(new Synonym("Test merge synonym", "TEST", null, Collections.<String>emptySet()));
		DefaultOntologyTerm termMod = new DefaultOntologyTerm("CARO:0001001", "neuron projection bundle", null, synonyms, null, null, null);
		assertEquals(LoadState.modifySuccess, handleTerm(termMod, Modification.modify, obodoc));
		assertClauseCount(6, "CARO:0001001", obodoc);
		
		assertEquals(LoadState.removeSuccess, handleTerm(term, Modification.remove, obodoc));
		assertEquals(LoadState.removeMissing, handleTerm(term, Modification.remove, obodoc));
	}

	private void assertClauseCount(int count, String id, OBODoc oboDoc) {
		Frame termFrame = oboDoc.getTermFrame(id);
		assertEquals(count, termFrame.getClauses().size());
	}
}
