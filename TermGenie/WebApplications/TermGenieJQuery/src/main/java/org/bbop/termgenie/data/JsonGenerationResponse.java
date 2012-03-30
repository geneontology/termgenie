package org.bbop.termgenie.data;

import java.util.List;

public class JsonGenerationResponse {

	private String generalError;
	private List<JsonValidationHint> errors;
	private List<JsonOntologyTerm> generatedTerms;
	private List<JsonTermTemplate> termTemplates;

	/**
	 * Default constructor.
	 */
	public JsonGenerationResponse() {
		super();
	}

	public JsonGenerationResponse(String generalError,
			List<JsonValidationHint> errors,
			List<JsonOntologyTerm> terms,
			List<JsonTermTemplate> termTemplates)
	{
		if (errors != null) {
			this.errors = errors;
		}
		if (terms != null) {
			this.generatedTerms = terms;
		}
		this.generalError = generalError;
		this.termTemplates = termTemplates;
	}

	/**
	 * @return the errors
	 */
	public List<JsonValidationHint> getErrors() {
		return errors;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(List<JsonValidationHint> errors) {
		this.errors = errors;
	}

	/**
	 * @return the generatedTerms
	 */
	public List<JsonOntologyTerm> getGeneratedTerms() {
		return generatedTerms;
	}

	/**
	 * @param generatedTerms the generatedTerms to set
	 */
	public void setGeneratedTerms(List<JsonOntologyTerm> generatedTerms) {
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

	/**
	 * @return the termTemplates
	 */
	public List<JsonTermTemplate> getTermTemplates() {
		return termTemplates;
	}

	/**
	 * @param termTemplates the termTemplates to set
	 */
	public void setTermTemplates(List<JsonTermTemplate> termTemplates) {
		this.termTemplates = termTemplates;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonGenerationResponse:{");
		if (generalError != null) {
			builder.append("generalError:");
			builder.append(generalError);
			builder.append(", ");
		}
		if (errors != null) {
			builder.append("errors:");
			builder.append(errors);
			builder.append(", ");
		}
		if (generatedTerms != null) {
			builder.append("generatedTerms:");
			builder.append(generatedTerms);
		}
		builder.append("}");
		return builder.toString();
	}

}
