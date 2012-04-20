package org.bbop.termgenie.ontology;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.oboformat.model.Frame;
import org.semanticweb.owlapi.model.OWLAxiom;


public class CommitInfo {
	
	public static class TermCommit {
		
		private String pattern;
		private Frame term;
		private Set<OWLAxiom> owlAxioms;
		private List<Pair<Frame, Set<OWLAxiom>>> changed;
		
		/**
		 * @param term
		 * @param owlAxioms
		 * @param changed
		 * @param pattern
		 */
		public TermCommit(Frame term, Set<OWLAxiom> owlAxioms, List<Pair<Frame, Set<OWLAxiom>>> changed, String pattern) {
			this.term = term;
			this.owlAxioms = owlAxioms;
			this.changed = changed;
			this.pattern = pattern;
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
		 * @return the owlAxioms
		 */
		public Set<OWLAxiom> getOwlAxioms() {
			return owlAxioms;
		}
		
		/**
		 * @param owlAxioms the owlAxioms to set
		 */
		public void setOwlAxioms(Set<OWLAxiom> owlAxioms) {
			this.owlAxioms = owlAxioms;
		}

		/**
		 * @return the changed
		 */
		public List<Pair<Frame, Set<OWLAxiom>>> getChanged() {
			return changed;
		}
		
		/**
		 * @param changed the changed to set
		 */
		public void setChanged(List<Pair<Frame, Set<OWLAxiom>>> changed) {
			this.changed = changed;
		}
		
		/**
		 * @return the pattern
		 */
		public String getPattern() {
			return pattern;
		}

		/**
		 * @param pattern the pattern to set
		 */
		public void setPattern(String pattern) {
			this.pattern = pattern;
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
