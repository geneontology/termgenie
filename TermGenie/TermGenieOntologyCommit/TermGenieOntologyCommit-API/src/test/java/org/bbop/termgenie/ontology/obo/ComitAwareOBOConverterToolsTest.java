package org.bbop.termgenie.ontology.obo;

import static org.bbop.termgenie.ontology.obo.ComitAwareOboTools.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools.LoadState;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;

public class ComitAwareOBOConverterToolsTest extends ResourceLoader {

	public ComitAwareOBOConverterToolsTest() {
		super(false);
	}

	@Test
	public void testHandleTerm() throws IOException {
		Frame term = createTermFrame("CARO:0001002", "test label 1");
		term.addClause(new Clause(OboFormatTag.TAG_IS_A, "CARO:0001001"));
		InputStream inputStream = loadResource("caro-mini-test.obo");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		OBOFormatParser parser = new OBOFormatParser();
		OBODoc obodoc = parser.parse(reader);
		assertEquals(LoadState.addSuccess, handleTerm(term, null, Modification.add, obodoc));
		assertEquals(LoadState.addRedundant, handleTerm(term, null, Modification.add, obodoc));
		assertClauseCount(3, "CARO:0001002", obodoc);
		assertEquals(LoadState.modifyRedundant, handleTerm(term, null, Modification.modify, obodoc));
		assertClauseCount(3, "CARO:0001002", obodoc);
		
		Frame termMod = createTermFrame("CARO:0001001", "neuron projection bundle");
		addSynonym(termMod, "Test merge synonym", "TEST", Collections.<String>emptyList());
		
		assertEquals(LoadState.modifySuccess, handleTerm(termMod, null, Modification.modify, obodoc));
		assertClauseCount(6, "CARO:0001001", obodoc);
		
		assertEquals(LoadState.removeSuccess, handleTerm(term, null, Modification.remove, obodoc));
		assertEquals(LoadState.removeMissing, handleTerm(term, null, Modification.remove, obodoc));
	}

	private void assertClauseCount(int count, String id, OBODoc oboDoc) {
		Frame termFrame = oboDoc.getTermFrame(id);
		assertEquals(count, termFrame.getClauses().size());
	}
}
