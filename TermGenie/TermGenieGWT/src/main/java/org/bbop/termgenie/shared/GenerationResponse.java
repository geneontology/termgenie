package org.bbop.termgenie.shared;

import java.util.Collection;

import org.bbop.termgenie.shared.FieldValidator.GWTValidationHint;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GenerationResponse implements IsSerializable {

	private String generalError;
	private GWTValidationHint[] errors;
	private String[] generatedTerms;
	
	/**
	 * Default constructor, required for {@link IsSerializable}.
	 */
	@SuppressWarnings("unused")
	private GenerationResponse() {
		super();
	}
	
	public GenerationResponse(String generalError, Collection<GWTValidationHint> errors, Collection<String> terms) {
		if (errors != null) {
			this.errors = errors.toArray(new GWTValidationHint[errors.size()]);
		}
		if (terms != null) {
			this.generatedTerms = terms.toArray(new String[terms.size()]);
		}
		this.generalError = generalError;
	}
	
	/**
	 * @return the errors
	 */
	public GWTValidationHint[] getErrors() {
		return errors;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(GWTValidationHint[] errors) {
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
