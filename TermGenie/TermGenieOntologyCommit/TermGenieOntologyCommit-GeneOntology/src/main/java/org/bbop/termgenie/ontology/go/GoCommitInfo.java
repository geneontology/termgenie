package org.bbop.termgenie.ontology.go;

import java.util.List;

import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.user.UserData;


public class GoCommitInfo extends CommitInfo {

	/**
	 * Create a commit
	 * 
	 * @param terms
	 * @param commitMessage
	 * @param userData
	 */
	public GoCommitInfo(List<CommitObject<TermCommit>> terms,
			String commitMessage,
			UserData userData)
	{
		super(terms, userData, CommitMode.internal, commitMessage, null, null);
	}

}
