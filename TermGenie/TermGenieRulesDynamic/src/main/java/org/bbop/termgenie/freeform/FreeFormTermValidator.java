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
	 * Get the list of additional relations supported by this validator. By
	 * default only is_a/subClassOf is supported.
	 * 
	 * @return list of relations
	 */
	public List<String> getAdditionalRelations();
	
	/**
	 * Validate the given request.
	 * 
	 * @param request
	 * @param requireLiteratureReference
	 * @param state
	 * @return response
	 */
	public FreeFormValidationResponse validate(FreeFormTermRequest request,
			boolean requireLiteratureReference,
			ProcessState state);
	
	
	/**
	 * Get the temporary identifier prefix for the validator.
	 * 
	 * @return prefix string
	 */
	public String getTempIdPrefix();

}
