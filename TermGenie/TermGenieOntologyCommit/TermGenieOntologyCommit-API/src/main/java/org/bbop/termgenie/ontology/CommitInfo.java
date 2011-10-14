package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;

import owltools.graph.OWLGraphWrapper.Synonym;


public class CommitInfo {

	private final List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms;
	
	public enum CommitMode {
		anonymus, internal, explicit
	}
	
	private final CommitMode commitMode;
	private final String termgenieUser;
	
	private final String username;
	private final String password;
	
	/**
	 * @param terms
	 * @param termgenieUser
	 * @param commitMode
	 * @param username
	 * @param password
	 */
	public CommitInfo(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			String termgenieUser,
			CommitMode commitMode,
			String username,
			String password)
	{
		super();
		this.terms = terms;
		this.termgenieUser = termgenieUser;
		this.commitMode = commitMode;
		this.username = username;
		this.password = password;
	}

	/**
	 * @return the terms
	 */
	public List<CommitObject<OntologyTerm<Synonym, IRelation>>> getTerms() {
		return terms;
	}

	/**
	 * @return the termgenieUser
	 */
	public String getTermgenieUser() {
		return termgenieUser;
	}

	/**
	 * @return the commitMode
	 */
	public CommitMode getCommitMode() {
		return commitMode;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
}
