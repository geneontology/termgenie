package org.bbop.termgenie.ontology.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class CommitHistoryItem {

	private int id;
	private int version; // used to detect conflicting updates, do not modify
	private Date date = null;
	private String commitMessage = null;
	private String email = null;
	private String savedBy = null;
	private boolean committed = false;

	private List<CommitedOntologyTerm> terms = null;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 *  Used to detect conflicting updates, do not modify.
	 * 
	 * @return the version
	 */
	@Version
	public int getVersion() {
		return version;
	}
	
	/**
	 * @return the date
	 */
	@Column
	@Basic(optional = false)
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the commitMessage
	 */
	@Column(length = Integer.MAX_VALUE)
	public String getCommitMessage() {
		return commitMessage;
	}

	/**
	 * @param commitMessage the commitMessage to set
	 */
	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
	/**
	 * @return the email
	 */
	@Column(length=1023) // increase default length from 255
	@Basic(optional=false)
	public String getEmail() {
		if(email==null){
			return "unassigned@geneontology.termgenie.org";
		}
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the committed
	 */
	@Column
	public boolean isCommitted() {
		return committed;
	}

	/**
	 * @param committed the committed to set
	 */
	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	/**
	 * @return the terms
	 */
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch=FetchType.EAGER)
	public List<CommitedOntologyTerm> getTerms() {
		return terms;
	}

	/**
	 * @param terms the terms to set
	 */
	public void setTerms(List<CommitedOntologyTerm> terms) {
		this.terms = terms;
	}

	/**
	 * @return savedBy
	 */
	@Column
	public String getSavedBy() {
		return savedBy;
	}

	/**
	 * @param savedBy the savedBy to set
	 */
	public void setSavedBy(String savedBy) {
		this.savedBy = savedBy;
	}

}
