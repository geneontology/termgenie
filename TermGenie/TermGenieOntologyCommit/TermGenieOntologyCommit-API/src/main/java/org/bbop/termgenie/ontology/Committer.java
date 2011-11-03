package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.tools.Pair;

/**
 * Methods for committing changes to an ontology
 */
public interface Committer {

	/**
	 * @param commitInfo
	 * @return CommitResult
	 * @throws CommitException
	 */
	public CommitResult commit(CommitInfo commitInfo) throws CommitException;
	
	/**
	 * Check if the labels are in recent commits;
	 * 
	 * @param labels
	 * @return matching id,label pairs
	 */
	public List<Pair<String, String>> checkRecentCommits(List<String> labels);

	public static class CommitResult {

		public static final CommitResult ERROR = new CommitResult(false, null, null, null);

		private final boolean success;
		private final String message;
		private final List<CommitObject<TermCommit>> terms;
		private final String diff;

		/**
		 * @param success
		 * @param message
		 * @param terms
		 * @param diff
		 */
		public CommitResult(boolean success,
				String message,
				List<CommitObject<TermCommit>> terms,
				String diff)
		{
			super();
			this.success = success;
			this.message = message;
			this.terms = terms;
			this.diff = diff;
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @return the diff
		 */
		public String getDiff() {
			return diff;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return the terms
		 */
		public List<CommitObject<TermCommit>> getTerms() {
			return terms;
		}
	}
}
