package org.bbop.termgenie.services.permissions;

import org.bbop.termgenie.core.Ontology;


public interface UserPermissions {

	public boolean allowCommitReview(String guid, Ontology ontology);
	
	/**
	 * Retrieve the commit user information for a user and ontology during commit review.
	 * 
	 * @param guid
	 * @param ontology
	 * @return {@link CommitUserData} or null
	 */
	public CommitUserData getCommitReviewUserData(String guid, Ontology ontology);
	
	public boolean allowCommit(String guid, Ontology ontology);
	
	/**
	 * Retrieve the commit user information for a user and ontology.
	 * 
	 * @param guid
	 * @param ontology
	 * @return {@link CommitUserData} or null
	 */
	public CommitUserData getCommitUserData(String guid, Ontology ontology);
	
	public interface CommitUserData {
		
		/**
		 * @return the username
		 */
		public String getUsername();
		
		/**
		 * @return the password
		 */
		public String getPassword();
		
		/**
		 * @return the screenname
		 */
		public String getScreenname();
	}
}
