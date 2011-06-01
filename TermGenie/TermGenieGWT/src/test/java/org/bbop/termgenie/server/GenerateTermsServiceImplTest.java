package org.bbop.termgenie.server;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.shared.GWTGenerationResponse;
import org.bbop.termgenie.shared.GWTPair;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.junit.Test;

public class GenerateTermsServiceImplTest {

	@SuppressWarnings("serial")
	private GenerateTermsService instance = new GenerateTermsServiceImpl() {

		@Override
		protected Collection<TermTemplate> requestTemplates(String ontology) {
			if ("test".equals(ontology)) {
				return TestTermTemplates.getTestTemplates();
			}
			return Collections.emptySet();
		}
		
	};
	
	private static class TestTermTemplates extends DefaultTermTemplates {
		static List<TermTemplate> getTestTemplates() {
			return defaultTemplates;
		}
	}
	
	@Test
	public void testGetAvailableGWTTermTemplates() {
		GWTTermTemplate[] termTemplates = instance.getAvailableGWTTermTemplates("test");
		assertNotNull(termTemplates);
		assertTrue(termTemplates.length > 0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGenerateTerms() {
		GWTGenerationResponse response = instance.generateTerms(null, null, true, null, null);
		assertNotNull(response.getGeneralError());
		
		response = instance.generateTerms("x", null, true, null, null);
		assertNotNull(response.getGeneralError());
		
		GWTPair<GWTTermTemplate, GWTTermGenerationParameter>[] allParameters = new GWTPair[0];
		response = instance.generateTerms("x", allParameters , true, null, null);
		assertNotNull(response.getGeneralError());
		
//		instance.generateTerms(OntologyLoadTests.GENE_ONTOLOGY_NAME, allParameters, commit, username, password);
		fail("Not yet implemented");
	}

}
