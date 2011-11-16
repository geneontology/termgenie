package org.bbop.termgenie.ontology.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.bbop.termgenie.ontology.CommitObject.Modification;

@Entity
public class SimpleCommitedOntologyTerm
{

	private int uuid;
	private String id;
	
	private String obo;

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
	 * @return the obo
	 */
	@Column(length = Integer.MAX_VALUE)
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
