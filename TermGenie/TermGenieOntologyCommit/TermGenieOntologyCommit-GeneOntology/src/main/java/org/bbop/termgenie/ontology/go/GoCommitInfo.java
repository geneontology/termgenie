package org.bbop.termgenie.ontology.go;

import java.util.List;

import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;


public class GoCommitInfo extends CommitInfo {

	/**
	 * Create a commit
	 * 
	 * @param terms
	 * @param termgenieUser
	 */
	public GoCommitInfo(List<CommitObject<TermCommit>> terms,
			String termgenieUser)
	{
		super(terms, termgenieUser, CommitMode.internal, null, null);
	}

}
