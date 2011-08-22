package org.bbop.termgenie.ontology.entities;

import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentMap;
import org.bbop.termgenie.core.Ontology.IRelation;

@Entity
public class CommitedOntologyTermRelation implements IRelation {

	private int id;
	private String source;
	private String target;
	private String targetLabel;
	private Map<String, String> properties;
	
	private int operation;

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
	 * @return the source
	 */
	@Override
	@Column
	@Basic(optional = false)
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	@Override
	@Column
	@Basic(optional = false)
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * @return the targetLabel
	 */
	@Override
	@Column
	@Basic(optional = false)
	public String getTargetLabel() {
		return targetLabel;
	}
	
	/**
	 * @param targetLabel the targetLabel to set
	 */
	public void setTargetLabel(String targetLabel) {
		this.targetLabel = targetLabel;
	}

	/**
	 * @return the properties
	 */
	@Override
	@PersistentMap
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return the operation
	 */
	@Column
	public int getOperation() {
		return operation;
	}
	
	/**
	 * @param operation the operation to set
	 */
	public void setOperation(int operation) {
		this.operation = operation;
	}
}
