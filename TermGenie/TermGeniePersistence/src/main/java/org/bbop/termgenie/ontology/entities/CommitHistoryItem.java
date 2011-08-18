package org.bbop.termgenie.ontology.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;

public class CommitHistoryItem {

	private int id;
	private Date date = null;
	private String user;

	private List<CommitedOntologyTerm> terms = null;
	private List<CommitedOntologyTermRelation> relations = null;

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
	@Column(length = Integer.MAX_VALUE)
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
	 * @return the terms
	 */
	@PersistentCollection
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
	 * @return the relations
	 */
	@PersistentCollection
	public List<CommitedOntologyTermRelation> getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(List<CommitedOntologyTermRelation> relations) {
		this.relations = relations;
	}

}
