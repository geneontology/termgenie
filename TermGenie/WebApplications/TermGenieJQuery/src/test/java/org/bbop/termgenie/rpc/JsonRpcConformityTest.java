package org.bbop.termgenie.rpc;

import static org.junit.Assert.*;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.authenticate.BrowserIdHandler;
import org.bbop.termgenie.services.authenticate.OpenIdRequestHandler;
import org.json.rpc.commons.TypeChecker;
import org.json.rpc.server.InjectingGsonTypeChecker;
import org.junit.Test;

public class JsonRpcConformityTest {

	@Test
	public void testMethodInterfaces() {
		TypeChecker checker = new InjectingGsonTypeChecker();

		assertTrue(checker.isValidInterface(GenerateTermsService.class, true));

		assertTrue(checker.isValidInterface(OntologyService.class, true));

		assertTrue(checker.isValidInterface(TermCommitService.class, true));

		assertTrue(checker.isValidInterface(SessionHandler.class, true));
		
		assertTrue(checker.isValidInterface(OpenIdRequestHandler.class, true));
		
		assertTrue(checker.isValidInterface(BrowserIdHandler.class, true));
	}
}
