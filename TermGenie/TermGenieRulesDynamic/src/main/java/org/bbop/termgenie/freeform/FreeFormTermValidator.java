package org.bbop.termgenie.freeform;

import java.util.List;

import org.bbop.termgenie.core.process.ProcessState;


/**
 *  Validator for a free form term request
 */
public interface FreeFormTermValidator {

	/**
	 * Get the list of OBO namespaces supported by this validator.
	 * 
	 * @return list of OBO namespaces
	 */
	public List<String> getOboNamespaces();
	
	/**
	 * Validate the given request.
	 * 
	 * @param request
	 * @param state
	 * @return response
	 */
	public FreeFormValidationResponse validate(FreeFormTermRequest request, ProcessState state);

}
