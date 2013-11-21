package org.bbop.termgenie.services;

import java.util.List;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonGenerationResponse;
import org.bbop.termgenie.data.JsonTermGenerationInput;
import org.bbop.termgenie.data.JsonTermTemplate;
import org.json.rpc.server.ProcessStateAware;

public interface GenerateTermsService extends AutoCompleteResources{

	/**
	 * Retrieve the available term templates.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @return gwtTermTemplates
	 */
	public List<JsonTermTemplate> availableTermTemplates(String sessionId);

	/**
	 * Generate terms and return the proposed terms for review.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param allParameters
	 * @param processState
	 * @return JsonGenerationResponse, detailing errors and possible terms.
	 */
	@ProcessStateAware
	public JsonGenerationResponse generateTerms(String sessionId,
			List<JsonTermGenerationInput> allParameters,
			ProcessState processState);
}
