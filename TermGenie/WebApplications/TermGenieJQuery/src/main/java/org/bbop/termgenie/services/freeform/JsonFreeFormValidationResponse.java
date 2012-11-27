package org.bbop.termgenie.services.freeform;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.freeform.FreeFormHint;
import org.bbop.termgenie.freeform.FreeFormValidationResponse;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;

import owltools.graph.OWLGraphWrapper;


public class JsonFreeFormValidationResponse {

	private String generalError;
	private List<FreeFormHint> errors;
	private List<FreeFormHint> warnings;
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
	 * @return the warnings
	 */
	public List<FreeFormHint> getWarnings() {
		return warnings;
	}
	
	/**
	 * @param warnings the warnings to set
	 */
	public void setWarnings(List<FreeFormHint> warnings) {
		this.warnings = warnings;
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
	
	static JsonFreeFormValidationResponse convert(FreeFormValidationResponse response, OWLGraphWrapper graph) {
		JsonFreeFormValidationResponse json = new JsonFreeFormValidationResponse();
		json.setGeneralError(response.getGeneralError());
		json.setErrors(response.getErrors());
		json.setWarnings(response.getWarnings());
		Pair<Frame,Set<OWLAxiom>> pair = response.getGeneratedTerm();
		if (pair != null) {
			JsonOntologyTerm term = JsonOntologyTerm.createJson(pair.getOne(), pair.getTwo(), null, graph, "freeform");
			json.setGeneratedTerm(term);
		}
		return json;
	}
}
