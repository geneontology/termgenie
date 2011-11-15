package org.bbop.termgenie.ontology.entities;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;
import org.bbop.termgenie.ontology.CommitObject.Modification;

@Entity
public class CommitedOntologyTerm
{

	private int uuid;
	private String id;
	private String label;
	
	private String obo;
	private List<CommitedOntologyTerm> changed;

	private Modification operation;

	/**
	 * @return the uuid
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(int uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the id
	 */
	@Column
	@Basic(optional = false)
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the label
	 */
	@Column
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
	 * @return the operation
	 */
	@Column
	@Enumerated(EnumType.STRING)
	public Modification getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Modification operation) {
		this.operation = operation;
	}

	/**
	 * @return the changed
	 */
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public List<CommitedOntologyTerm> getChanged() {
		return changed;
	}

	/**
	 * @param changed the changed to set
	 */
	public void setChanged(List<CommitedOntologyTerm> changed) {
		this.changed = changed;
	}

	
	/**
	 * @return the obo
	 */
	@Column
	@Basic(optional = false)
	public String getObo() {
		return obo;
	}
	
	/**
	 * @param obo the obo to set
	 */
	public void setObo(String obo) {
		this.obo = obo;
	}
}
