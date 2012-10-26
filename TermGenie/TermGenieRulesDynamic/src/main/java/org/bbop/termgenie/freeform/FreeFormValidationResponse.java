package org.bbop.termgenie.freeform;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * General response to a free form request.
 */
public class FreeFormValidationResponse {

	private String generalError;
	private List<FreeFormHint> errors;
	private Pair<Frame, Set<OWLAxiom>> generatedTerm;
	
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
	public List<FreeFormHint> getErrors() {
		return errors;
	}
	
	/**
	 * @param errors the errors to set
	 */
	public void setErrors(List<FreeFormHint> errors) {
		this.errors = errors;
	}
	
	/**
	 * @return the generatedTerms
	 */
	public Pair<Frame, Set<OWLAxiom>> getGeneratedTerm() {
		return generatedTerm;
	}
	
	/**
	 * @param generatedTerm the generatedTerms to set
	 */
	public void setGeneratedTerm(Pair<Frame, Set<OWLAxiom>> generatedTerm) {
		this.generatedTerm = generatedTerm;
	}
	
}
