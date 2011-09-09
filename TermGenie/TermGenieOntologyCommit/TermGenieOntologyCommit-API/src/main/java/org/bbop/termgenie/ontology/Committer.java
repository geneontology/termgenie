package org.bbop.termgenie.ontology;


/**
 * Methods for committing changes to an ontology
 */
public interface Committer {

	/**
	 * @param commitInfo
	 * @return true, if the transaction for the commit was successful
	 * @throws CommitException
	 */
	public boolean commit(CommitInfo commitInfo) throws CommitException;
	
}
