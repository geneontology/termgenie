package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonGenerationResponse;
import org.bbop.termgenie.data.JsonTermGenerationInput;
import org.bbop.termgenie.data.JsonTermTemplate;

public interface GenerateTermsService {

	/**
	 * Retrieve the available term templates parameters for the given ontology;
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param ontology
	 * @return gwtTermTemplates
	 */
	public JsonTermTemplate[] availableTermTemplates(String sessionId, String ontology);

	/**
	 * Generate terms and return the proposed terms for review.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param ontology
	 * @param allParameters
	 * @return JsonGenerationResponse, detailing errors and possible terms.
	 */
	public JsonGenerationResponse generateTerms(String sessionId,
			String ontology,
			JsonTermGenerationInput[] allParameters);
}
