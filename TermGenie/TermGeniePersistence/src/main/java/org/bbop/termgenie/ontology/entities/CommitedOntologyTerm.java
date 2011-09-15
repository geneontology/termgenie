package org.bbop.termgenie.ontology.entities;

import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.PersistentMap;

@Entity
public class CommitedOntologyTerm {

	private int uuid;
	private String id;
	private String label;
	private String definition;
	private List<CommitedOntologyTermSynonym> synonyms;
	private List<String> defXRef;
	private List<CommitedOntologyTermRelation> relations;
	private Map<String, String> metaData;

	private int operation;

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
	 * @return the definition
	 */
	@Column
	@Basic
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the synonyms
	 */
	@PersistentCollection
	public List<CommitedOntologyTermSynonym> getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<CommitedOntologyTermSynonym> synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return the defXRef
	 */
	@PersistentCollection
	public List<String> getDefXRef() {
		return defXRef;
	}

	/**
	 * @param defXRef the defXRef to set
	 */
	public void setDefXRef(List<String> defXRef) {
		this.defXRef = defXRef;
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

	/**
	 * @return the metaData
	 */
	@PersistentMap
	public Map<String, String> getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
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
