package org.bbop.termgenie.services.review;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;

import owltools.graph.OWLGraphWrapper;

public class JsonCommitReviewCommitResult extends JsonResult {

	private List<JsonCommitDetails> details;

	/**
	 * @return the details
	 */
	public List<JsonCommitDetails> getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(List<JsonCommitDetails> details) {
		this.details = details;
	}

	public static class JsonCommitDetails extends JsonResult {

		private int historyId;
		private List<JsonOntologyTerm> terms;
		private String diff;

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
		public List<JsonOntologyTerm> getTerms() {
			return terms;
		}

		/**
		 * @param terms the terms to set
		 */
		public void setTerms(List<JsonOntologyTerm> terms) {
			this.terms = terms;
		}
		
		/**
		 * @return the diff
		 */
		public String getDiff() {
			return diff;
		}

		/**
		 * @param diff the diff to set
		 */
		public void setDiff(String diff) {
			this.diff = diff;
		}
	}

	static JsonCommitReviewCommitResult error(String message) {
		JsonCommitReviewCommitResult result = new JsonCommitReviewCommitResult();
		result.setSuccess(false);
		result.setMessage(message);
		return result;
	}

	static JsonCommitReviewCommitResult success(List<Integer> ids, List<CommitResult> commits, OntologyTaskManager manager) throws InvalidManagedInstanceException {
		GenerateSuccessTask task = new GenerateSuccessTask(ids, commits);
		manager.runManagedTask(task);
		return task.result;
	}
	
	private static class GenerateSuccessTask extends OntologyTask {
		
		private final List<Integer> ids;
		private final List<CommitResult> commits;
		private JsonCommitReviewCommitResult result;

		/**
		 * @param ids
		 * @param commits
		 */
		GenerateSuccessTask(List<Integer> ids, List<CommitResult> commits) {
			super();
			this.ids = ids;
			this.commits = commits;
		}
		
		

		@Override
		public void runCatching(OWLGraphWrapper graph) {
			result = new JsonCommitReviewCommitResult();
			result.setSuccess(true);
			List<JsonCommitDetails> details = new ArrayList<JsonCommitDetails>(commits.size());
			for (int i = 0; i < ids.size(); i++) {
				CommitResult commitResult = commits.get(i);
				JsonCommitDetails json = new JsonCommitDetails();
				json.setSuccess(commitResult.isSuccess());
				json.setMessage(commitResult.getMessage());
				json.setHistoryId(ids.get(i));
				json.setTerms(convert(commitResult.getTerms(),graph));
				json.setDiff(commitResult.getDiff());
				details.add(json);
			}
			result.setDetails(details);
		}

	}

	private static List<JsonOntologyTerm> convert(List<CommitObject<TermCommit>> terms, OWLGraphWrapper wrapper)
	{
		if (terms == null) {
			return null;
		}
		List<JsonOntologyTerm> jsonTerms = new ArrayList<JsonOntologyTerm>();
		for (CommitObject<TermCommit> commitObject : terms) {
			TermCommit object = commitObject.getObject();
			jsonTerms.add(JsonOntologyTerm.createJson(object.getTerm(), object.getOwlAxioms(), object.getChanged(), wrapper, object.getPattern()));
		}
		return jsonTerms;
	}
}
