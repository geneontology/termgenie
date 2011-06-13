package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonGenerationResponse;
import org.bbop.termgenie.data.JsonTermGenerationInput;
import org.bbop.termgenie.data.JsonTermTemplate;

public interface GenerateTermsService {

	/**
	 * Retrieve the available term templates parameters for the given ontology;
	 * 
	 * @param ontology
	 * @return gwtTermTemplates
	 */
	public JsonTermTemplate[] getAvailableTermTemplates(String ontology);
	
	/**
	 * Generate terms, and commit it to the ontology
	 * 
	 * @param ontology
	 * @param allParameters
	 * @param commit
	 * @param username
	 * @param password
	 * 
	 * @return if the operation succeeded
	 */
	public JsonGenerationResponse generateTerms(String ontology,
			JsonTermGenerationInput[] allParameters,
			boolean commit, String username, String password);
}
