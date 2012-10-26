package org.bbop.termgenie.freeform;


/**
 *  Validator for a free form term request
 */
public interface FreeFormTermValidator {

	/**
	 * Validate the given request.
	 * 
	 * @param request
	 * @return response
	 */
	public FreeFormValidationResponse validate(FreeFormTermRequest request);

}
