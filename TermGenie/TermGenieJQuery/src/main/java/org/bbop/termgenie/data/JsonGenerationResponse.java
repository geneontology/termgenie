package org.bbop.termgenie.data;

import java.util.Collection;


public class JsonGenerationResponse {

	private String generalError;
	private JsonValidationHint[] errors;
	private String[] generatedTerms;
	
	/**
	 * Default constructor.
	 */
	@SuppressWarnings("unused")
	private JsonGenerationResponse() {
		super();
	}
	
	public JsonGenerationResponse(String generalError, Collection<JsonValidationHint> errors, Collection<String> terms) {
		if (errors != null) {
			this.errors = errors.toArray(new JsonValidationHint[errors.size()]);
		}
		if (terms != null) {
			this.generatedTerms = terms.toArray(new String[terms.size()]);
		}
		this.generalError = generalError;
	}
	
	/**
	 * @return the errors
	 */
	public JsonValidationHint[] getErrors() {
		return errors;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(JsonValidationHint[] errors) {
		this.errors = errors;
	}

	/**
	 * @return the generatedTerms
	 */
	public String[] getGeneratedTerms() {
		return generatedTerms;
	}

	/**
	 * @param generatedTerms the generatedTerms to set
	 */
	public void setGeneratedTerms(String[] generatedTerms) {
		this.generatedTerms = generatedTerms;
	}

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
}
