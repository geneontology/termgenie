package org.bbop.termgenie.ontology;

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
	
	
	public static class CommitResult {
		
		
		public static final CommitResult ERROR = new CommitResult(false, null);
		
		private final boolean success;
		private final String diff;
		
		/**
		 * @param success
		 * @param diff
		 */
		public CommitResult(boolean success, String diff) {
			super();
			this.success = success;
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
	}
}
