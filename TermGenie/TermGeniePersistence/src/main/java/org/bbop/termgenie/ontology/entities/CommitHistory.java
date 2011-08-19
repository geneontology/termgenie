package org.bbop.termgenie.ontology.entities;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;


/**
 * Wrapper for a commit history for an ontology.
 */
@Entity
public class CommitHistory {

	private String ontology;

	private List<CommitHistoryItem> items;

	/**
	 * @return the ontology
	 */
	@Id
	public String getOntology() {
		return ontology;
	}

	/**
	 * @param ontology the ontology to set
	 */
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	/**
	 * @return the items
	 */
	@PersistentCollection
	public List<CommitHistoryItem> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<CommitHistoryItem> items) {
		this.items = items;
	}

}
