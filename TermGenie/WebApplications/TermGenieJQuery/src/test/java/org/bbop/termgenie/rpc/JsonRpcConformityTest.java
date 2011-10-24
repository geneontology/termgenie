package org.bbop.termgenie.rpc;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.SessionHandler;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.authenticate.BrowserIdHandler;
import org.bbop.termgenie.services.authenticate.OpenIdRequestHandler;
import org.bbop.termgenie.services.review.TermCommitReviewService;
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
		
		assertTrue(checker.isValidInterface(TermCommitReviewService.class, true));
	}
	
	@Test
	public void testGenericCollectionsAndMap() {
		TypeChecker checker = new InjectingGsonTypeChecker();
		
		boolean validType = checker.isValidInterface(TestGenericInterface.class, true);
		assertTrue(validType);
	}
	
	public static interface TestGenericInterface {

		public String getSimple();
		
		public List<String> getList();
		
		public void setList(List<String> list);
		
		public Set<String> getSet();
		
		public void setSet(Set<String> list);
		
		public Map<String, String> getMap();
		
		public void setMap(Map<String, String> map);
	}
}
