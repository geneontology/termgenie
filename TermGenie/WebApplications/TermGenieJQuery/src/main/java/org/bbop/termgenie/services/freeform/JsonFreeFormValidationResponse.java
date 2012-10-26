package org.bbop.termgenie.services.freeform;

import java.util.List;

import org.bbop.termgenie.data.JsonOntologyTerm;


public class JsonFreeFormValidationResponse {

	private String generalError;
	private List<JsonFreeFormHint> errors;
	private JsonOntologyTerm generatedTerm;
	
	/**
	 * @return the generalError
	 */
	public String getGeneralError() {
		return generalError;
	}
	
	/**
	 * @param generalError the generalError to set
	 */
	public void setGeneralError(String generalError) {
		this.generalError = generalError;
	}
	
	/**
	 * @return the errors
	 */
	public List<JsonFreeFormHint> getErrors() {
		return errors;
	}
	
	/**
	 * @param errors the errors to set
	 */
	public void setErrors(List<JsonFreeFormHint> errors) {
		this.errors = errors;
	}
	
	/**
	 * @return the generatedTerms
	 */
	public JsonOntologyTerm getGeneratedTerm() {
		return generatedTerm;
	}
	
	/**
	 * @param generatedTerm the generatedTerms to set
	 */
	public void setGeneratedTerm(JsonOntologyTerm generatedTerm) {
		this.generatedTerm = generatedTerm;
	}
}
