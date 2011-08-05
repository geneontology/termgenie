package org.bbop.termgenie.ontology.entities;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;

public class CommitedOntologyTermSynonym {
	
	private int id;
	private String label;
	private String scope;
	private String category;
	private Set<String>  xrefs;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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
	 * @return the label
	 */
	@Column(length=Integer.MAX_VALUE)
	@Basic(optional=false)
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the scope
	 */
	@Column
	@Basic(optional=true)
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the category
	 */
	@Column
	@Basic(optional=true)
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the xrefs
	 */
	@PersistentCollection
	public Set<String> getXrefs() {
		return xrefs;
	}

	/**
	 * @param xrefs the xrefs to set
	 */
	public void setXrefs(Set<String> xrefs) {
		this.xrefs = xrefs;
	}
}
