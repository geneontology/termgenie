package org.bbop.termgenie.freeform;

import org.bbop.termgenie.core.process.ProcessState;


/**
 *  Validator for a free form term request
 */
public interface FreeFormTermValidator {

	/**
	 * Validate the given request.
	 * 
	 * @param request
	 * @param state
	 * @return response
	 */
	public FreeFormValidationResponse validate(FreeFormTermRequest request, ProcessState state);

}
