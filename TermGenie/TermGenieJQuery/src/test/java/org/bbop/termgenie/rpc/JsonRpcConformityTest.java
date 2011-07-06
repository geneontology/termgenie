package org.bbop.termgenie.rpc;

import static org.junit.Assert.*;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.json.rpc.commons.GsonTypeChecker;
import org.json.rpc.commons.TypeChecker;
import org.junit.Test;

public class JsonRpcConformityTest {

	@Test
	public void testMethodInterfaces() {
		TypeChecker checker = new GsonTypeChecker();
		
		assertTrue(checker.isValidInterface(GenerateTermsService.class, true));
		
		assertTrue(checker.isValidInterface(OntologyService.class, true));
		
		assertTrue(checker.isValidInterface(TermCommitService.class, true));
		
		assertTrue(checker.isValidInterface(SessionHandler.class, true));
	}
}
