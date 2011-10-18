package org.bbop.termgenie.services.review;

import java.util.List;

import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.Committer.CommitResult;

public class JsonCommitReviewCommitResult extends JsonResult {

	private JsonResult[] details;

	/**
	 * @return the details
	 */
	public JsonResult[] getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(JsonResult[] details) {
		this.details = details;
	}
	
	static JsonCommitReviewCommitResult error(String message) {
		JsonCommitReviewCommitResult result = new JsonCommitReviewCommitResult();
		result.setSuccess(false);
		result.setMessage(message);
		return result;
	}
	
	static JsonCommitReviewCommitResult success(List<CommitResult> commits) {
		JsonCommitReviewCommitResult result = new JsonCommitReviewCommitResult();
		result.setSuccess(true);
		JsonResult[] details = new JsonResult[commits.size()];
		for (int i = 0; i < details.length; i++) {
			CommitResult commitResult = commits.get(i);
			JsonResult json = new JsonResult();
			json.setSuccess(commitResult.isSuccess());
			json.setMessage(commitResult.getMessage());
		}
		result.setDetails(details);
		return result;
	}
}
