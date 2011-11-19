package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.user.UserData;
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
	private final UserData userData;
	
	private final String commitMessage;
	
	private final String username;
	private final String password;
	
	/**
	 * @param terms
	 * @param userData
	 * @param commitMode
	 * @param commitMessage
	 * @param username
	 * @param password
	 */
	public CommitInfo(List<CommitObject<TermCommit>> terms,
			UserData userData,
			CommitMode commitMode,
			String commitMessage,
			String username,
			String password)
	{
		super();
		this.terms = terms;
		this.userData = userData;
		this.commitMode = commitMode;
		this.commitMessage = commitMessage;
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

	/**
	 * @return the userData
	 */
	public UserData getUserData() {
		return userData;
	}
	
	/**
	 * @return the commitMessage
	 */
	public String getCommitMessage() {
		return commitMessage;
	}
	
}
