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
	public JsonTermTemplate[] availableTermTemplates(String ontology);
	
	/**
	 * Generate terms and return the proposed terms for review.
	 * 
	 * @param ontology
	 * @param allParameters
	 * 
	 * @return JsonGenerationResponse, detailing errors and possible terms.  
	 */
	public JsonGenerationResponse generateTerms(String ontology,
			JsonTermGenerationInput[] allParameters);
}
