package org.bbop.termgenie.services.review;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer.CommitResult;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class JsonCommitReviewCommitResult extends JsonResult {

	private JsonCommitDetails[] details;

	/**
	 * @return the details
	 */
	public JsonCommitDetails[] getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(JsonCommitDetails[] details) {
		this.details = details;
	}
	
	public static class JsonCommitDetails extends JsonResult {
		
		private int historyId;
		private JsonOntologyTerm[] terms;
		
		/**
		 * @return the historyId
		 */
		public int getHistoryId() {
			return historyId;
		}
		
		/**
		 * @param historyId the historyId to set
		 */
		public void setHistoryId(int historyId) {
			this.historyId = historyId;
		}
		
		/**
		 * @return the terms
		 */
		public JsonOntologyTerm[] getTerms() {
			return terms;
		}
		
		/**
		 * @param terms the terms to set
		 */
		public void setTerms(JsonOntologyTerm[] terms) {
			this.terms = terms;
		}
	}
	
	static JsonCommitReviewCommitResult error(String message) {
		JsonCommitReviewCommitResult result = new JsonCommitReviewCommitResult();
		result.setSuccess(false);
		result.setMessage(message);
		return result;
	}
	
	static JsonCommitReviewCommitResult success(List<Integer> ids, List<CommitResult> commits) {
		JsonCommitReviewCommitResult result = new JsonCommitReviewCommitResult();
		result.setSuccess(true);
		JsonCommitDetails[] details = new JsonCommitDetails[commits.size()];
		for (int i = 0; i < details.length; i++) {
			CommitResult commitResult = commits.get(i);
			JsonCommitDetails json = new JsonCommitDetails();
			json.setSuccess(commitResult.isSuccess());
			json.setMessage(commitResult.getMessage());
			json.setHistoryId(ids.get(i));
			json.setTerms(convert(commitResult.getTerms()));
			details[i] = json;
		}
		result.setDetails(details);
		return result;
	}
	
	private static JsonOntologyTerm[] convert(List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms) {
		if (terms == null) {
			return null;
		}
		List<JsonOntologyTerm> jsonTerms = new ArrayList<JsonOntologyTerm>();
		for (CommitObject<OntologyTerm<ISynonym, IRelation>> commitObject : terms) {
			jsonTerms.add(JsonOntologyTerm.convert(commitObject.getObject()));	
		}
		return jsonTerms.toArray(new JsonOntologyTerm[jsonTerms.size()]);
	}
}
