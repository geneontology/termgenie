package org.bbop.termgenie.ontology.entities;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class CommitedOntologyTerm extends SimpleCommitedOntologyTerm
{
	private String label;
	private String pattern;
	
	private List<SimpleCommitedOntologyTerm> changed;

	/**
	 * @return the label
	 */
	@Column(length=Integer.MAX_VALUE)
	@Basic(optional = false)
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
	 * @return the changed
	 */
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public List<SimpleCommitedOntologyTerm> getChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(List<SimpleCommitedOntologyTerm> changed) {
		this.changed = changed;
	}
	
	/**
	 * @return the pattern
	 */
	@Column(length=1023) // increase default length from 255
	@Basic(optional=true)
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
