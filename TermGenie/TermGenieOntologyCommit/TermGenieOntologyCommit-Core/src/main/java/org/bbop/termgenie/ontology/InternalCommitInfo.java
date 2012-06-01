package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.user.UserData;


public class InternalCommitInfo extends CommitInfo {

	/**
	 * Create a commit
	 * 
	 * @param terms
	 * @param commitMessage
	 * @param userData
	 * @param sendConfirmationEMail
	 */
	public InternalCommitInfo(List<CommitObject<TermCommit>> terms,
			String commitMessage,
			UserData userData,
			boolean sendConfirmationEMail)
	{
		super(terms, userData, CommitMode.internal, commitMessage, null, null, sendConfirmationEMail);
	}

}
