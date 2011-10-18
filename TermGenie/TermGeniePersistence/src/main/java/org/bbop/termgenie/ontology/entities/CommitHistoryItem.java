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

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class CommitHistoryItem {

	private int id;
	private Date date = null;
	private String user;
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
	 * @return the user
	 */
	@Column(length = Integer.MAX_VALUE, name="termgenieuser")
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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

}
