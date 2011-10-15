package org.bbop.termgenie.ontology.entities;

import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.PersistentMap;
import org.bbop.termgenie.core.Ontology.OntologyTerm;

@Entity
public class CommitedOntologyTerm implements
		OntologyTerm<CommitedOntologyTermSynonym, CommitedOntologyTermRelation>
{

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
	@Override
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
	@Override
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
	@Override
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
	@Override
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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
	@Override
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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
	@Override
	@PersistentCollection(elementCascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
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
	@Override
	@PersistentMap(elementCascade = { CascadeType.ALL },
			keyCascade = { CascadeType.ALL },
			fetch = FetchType.EAGER)
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
