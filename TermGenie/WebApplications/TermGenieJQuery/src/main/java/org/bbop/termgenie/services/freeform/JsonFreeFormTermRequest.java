package org.bbop.termgenie.services.freeform;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.freeform.FreeFormTermRequest;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class JsonFreeFormTermRequest implements FreeFormTermRequest {

	private String label;
	private String namespace;

	private String definition;
	private List<String> dbxrefs;
	
	private String comment;

	private List<String> isA;
	
	private Map<String, List<String>> additionalRelations;

	private List<JsonSynonym> synonyms;
	
	private List<Xref> xrefs;

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	@Override
	public List<String> getDbxrefs() {
		return dbxrefs;
	}

	/**
	 * @param dbxrefs the dbxrefs to set
	 */
	public void setDbxrefs(List<String> dbxrefs) {
		this.dbxrefs = dbxrefs;
	}
	
	/**
	 * @return the comment
	 */
	@Override
	public String getComment() {
		return comment;
	}
	
	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public List<String> getIsA() {
		return isA;
	}

	/**
	 * @param isA the isA to set
	 */
	public void setIsA(List<String> isA) {
		this.isA = isA;
	}

	/**
	 * @return the additionalRelations
	 */
	@Override
	public Map<String, List<String>> getAdditionalRelations() {
		return additionalRelations;
	}
	
	/**
	 * @param additionalRelations the additionalRelations to set
	 */
	public void setAdditionalRelations(Map<String, List<String>> additionalRelations) {
		this.additionalRelations = additionalRelations;
	}

	/**
	 * @return the synonyms
	 */
	public List<JsonSynonym> getSynonyms() {
		return synonyms;
	}
	
	@Override
	public List<? extends ISynonym> getISynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<JsonSynonym> synonyms) {
		this.synonyms = synonyms;
	}
	
	/**
	 * @return the xrefs
	 */
	@Override
	public List<Xref> getXrefs() {
		return xrefs;
	}
	
	/**
	 * @param xrefs the xrefs to set
	 */
	public void setXrefs(List<Xref> xrefs) {
		this.xrefs = xrefs;
	}

}
