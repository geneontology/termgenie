package org.bbop.termgenie.ontology;

import java.util.List;

import org.obolibrary.oboformat.model.Frame;


public class CommitInfo {
	
	public static class TermCommit {
		
		private Frame term;
		private List<Frame> changed;
		
		/**
		 * @param term
		 * @param changed
		 */
		public TermCommit(Frame term, List<Frame> changed) {
			this.term = term;
			this.changed = changed;
		}

		/**
		 * @return the term
		 */
		public Frame getTerm() {
			return term;
		}
		
		/**
		 * @param term the term to set
		 */
		public void setTerm(Frame term) {
			this.term = term;
		}
		
		/**
		 * @return the changed
		 */
		public List<Frame> getChanged() {
			return changed;
		}
		
		/**
		 * @param changed the changed to set
		 */
		public void setChanged(List<Frame> changed) {
			this.changed = changed;
		}
	}

	private final List<CommitObject<TermCommit>> terms;
	
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
	public CommitInfo(List<CommitObject<TermCommit>> terms,
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
	public List<CommitObject<TermCommit>> getTerms() {
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
